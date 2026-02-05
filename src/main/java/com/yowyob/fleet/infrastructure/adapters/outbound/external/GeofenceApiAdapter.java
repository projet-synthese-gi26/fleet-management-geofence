package com.yowyob.fleet.infrastructure.adapters.outbound.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.fleet.domain.model.GeofenceZone;
import com.yowyob.fleet.domain.ports.out.ExternalGeofencePort;
import com.yowyob.fleet.infrastructure.adapters.outbound.external.client.GeofenceApiClient;
import com.yowyob.fleet.infrastructure.adapters.outbound.external.client.GeofenceAuthClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeofenceApiAdapter implements ExternalGeofencePort {

    private final GeofenceApiClient apiClient;
    private final GeofenceAuthClient authClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${application.geofence-system-user.username}")
    private String systemUser;

    @Value("${application.geofence-system-user.password}")
    private String systemPass;

    private Mono<String> cachedToken;

    @Override
    public Mono<String> getSystemToken() {
        if (cachedToken == null) {
            Map<String, String> loginReq = Map.of("type", "username", "username", systemUser, "password", systemPass);
            cachedToken = authClient.login(loginReq)
                    .map(res -> "Bearer " + (res.containsKey("token") ? res.get("token") : res.get("accessToken")))
                    .cache(Duration.ofHours(24));
        }
        return cachedToken;
    }

    private String resolveShortType(String type) {
        if (type == null)
            return "p";
        String t = type.toLowerCase();
        if (t.equals("c") || t.contains("circle"))
            return "c";
        return "p";
    }

    @Override
    public Mono<Void> synchronizeZone(GeofenceZone zone) {
        return getSystemToken().flatMap(token -> {
            Map<String, Object> request = buildGeofenceMap(zone);
            return apiClient.createZone(request, token)
                    .doOnSuccess(v -> log.info("✅ Zone {} synchronisée", zone.name()))
                    .then();
        });
    }

    @Override
    public Mono<java.util.UUID> createRemoteZone(GeofenceZone zone) {
        return getSystemToken().flatMap(token -> {
            Map<String, Object> request = buildGeofenceMap(zone);
            return apiClient.createZone(request, token)
                    .map(resp -> {
                        // Try to extract id from response map
                        if (resp == null)
                            return null;
                        Object idObj = resp.getOrDefault("id", resp.get("zoneId"));
                        if (idObj == null)
                            return null;
                        try {
                            return java.util.UUID.fromString(idObj.toString());
                        } catch (Exception e) {
                            log.warn("Impossible de parser l'id retourné par l'API externe: {}", idObj);
                            return null;
                        }
                    })
                    .flatMap(id -> id == null ? Mono.empty() : Mono.just(id));
        });
    }

    @Override
    public Mono<Void> updateRemoteZone(String type, UUID id, Map<String, Object> updates) {
        String shortType = resolveShortType(type);
        return getSystemToken().flatMap(token -> apiClient.updateZone(shortType, id, updates, token)
                .doOnSuccess(v -> log.info("🔄 Zone {} mise à jour", id)));
    }

    @Override
    public Mono<Void> deleteRemoteZone(String type, UUID zoneId) {
        String shortType = resolveShortType(type);
        return getSystemToken().flatMap(token -> apiClient.deleteZone(shortType, zoneId, token));
    }

@Override
public Mono<Map<String, Object>> getRemoteZoneDetails(String type, UUID id) {
    String shortType = resolveShortType(type);
    log.info("🌐 [EXTERNAL CALL] Fetching zone details. Type: {}, ID: {}", shortType, id);
    
    return getSystemToken()
        .flatMap(token -> {
            log.debug("🔑 Using Token: {}...", token.substring(0, 15));
            return apiClient.getZoneById(shortType, id, token)
                .doOnNext(node -> log.info("✅ Response received for zone {}", id))
                .map(node -> objectMapper.convertValue(node, new TypeReference<Map<String, Object>>() {}))
                .onErrorResume(e -> {
                    // On capture l'erreur réelle pour ne pas l'étouffer
                    if (e instanceof org.springframework.web.reactive.function.client.WebClientResponseException ex) {
                        log.error("❌ Geofence Service Error: {} - Body: {}", 
                                ex.getStatusCode(), ex.getResponseBodyAsString());
                        return Mono.error(new RuntimeException("Erreur externe: " + ex.getResponseBodyAsString()));
                    }
                    return Mono.error(e);
                });
        });
}

@Override
@SuppressWarnings("unchecked")
public Mono<List<Map<String, Object>>> listRemoteZones(String category) {
    log.info("🌐 [EXTERNAL CALL] Listing zones for category: {}", category);
    
    return getSystemToken().flatMap(token -> {
        Mono<JsonNode> response;
        if ("circles".equalsIgnoreCase(category)) response = apiClient.getCircles(token);
        else if ("polygons".equalsIgnoreCase(category)) response = apiClient.getPolygons(token);
        else response = apiClient.getAllZones(token);

        TypeReference<Map<String, Object>> mapType = new TypeReference<>() {};

        return response.map(node -> {
            List<Map<String, Object>> result = new ArrayList<>();
            
            // 1. Si c'est déjà un tableau [{}, {}]
            if (node.isArray()) {
                node.forEach(item -> result.add(objectMapper.convertValue(item, mapType)));
            } 
            // 2. Si c'est un objet, on cherche les clés connues
            else if (node.isObject()) {
                JsonNode listNode = null;
                if (node.has("content")) listNode = node.get("content");
                else if (node.has("zones")) listNode = node.get("zones");
                else if (node.has("data")) listNode = node.get("data");

                if (listNode != null && listNode.isArray()) {
                    listNode.forEach(item -> result.add(objectMapper.convertValue(item, mapType)));
                } else {
                    // Si c'est un objet simple sans liste (une seule zone), on l'ajoute
                    result.add(objectMapper.convertValue(node, mapType));
                }
            }
            
            log.info("📊 Zones trouvées après extraction : {}", result.size());
            return result;
        });
    });
}

    @Override
    public Mono<Map<String, Object>> fetchRemoteAlerts(int page, int size) {
        return getSystemToken().flatMap(token -> apiClient.getAlerts(page, size, token));
    }

    @Override
    public Mono<String> checkPointInZone(UUID zoneId, Double lat, Double lng) {
        return getSystemToken().flatMap(token -> apiClient.checkPoint(zoneId.toString(), lat, lng, token));
    }

    // Helper pour construire le JSON complexe attendu par Geofence
    private Map<String, Object> buildGeofenceMap(GeofenceZone zone) {
        Map<String, Object> request = new HashMap<>();
        request.put("title", zone.name());
        request.put("description", zone.description());
        request.put("isTemporalEnabled", Boolean.TRUE.equals(zone.getIsTemporalEnabled()));
        request.put("isConditionalEnabled", Boolean.TRUE.equals(zone.getIsConditionalEnabled()));
        request.put("startTime", zone.startTime());
        request.put("endTime", zone.endTime());

        if ("CIRCLE".equalsIgnoreCase(zone.zoneType())) {
            request.put("type", "circle");
            request.put("center", Map.of("coordinates", Arrays.asList(zone.centerLongitude(), zone.centerLatitude())));
            request.put("radius", zone.radius());
        } else {
            request.put("type", "polygon");
            List<List<Double>> ring = new ArrayList<>(zone.vertices().stream()
                    .map(v -> Arrays.asList(v.longitude(), v.latitude())).toList());
            if (!ring.isEmpty() && !ring.get(0).equals(ring.get(ring.size() - 1)))
                ring.add(ring.get(0));
            request.put("polygon", Map.of("type", "Polygon", "coordinates", List.of(ring)));
        }
        return request;
    }
}