package com.yowyob.fleet.infrastructure.adapters.outbound.external;

import com.yowyob.fleet.domain.model.GeofenceZone;
import com.yowyob.fleet.domain.ports.out.ExternalGeofencePort;
import com.yowyob.fleet.infrastructure.adapters.outbound.external.client.GeofenceApiClient;
import com.yowyob.fleet.infrastructure.adapters.outbound.external.client.GeofenceAuthClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        if (type == null) return "p";
        String t = type.toLowerCase();
        if (t.equals("c") || t.contains("circle")) return "c";
        return "p";
    }

    @Override
    public Mono<Void> synchronizeZone(GeofenceZone zone) {
        return getSystemToken().flatMap(token -> {
            Map<String, Object> request = buildGeofenceMap(zone);
            return apiClient.createZone(request, token)
                    .doOnSuccess(v -> log.info("✅ Zone {} synchronisée", zone.name()));
        });
    }

    @Override
    public Mono<Void> updateRemoteZone(String type, UUID id, Map<String, Object> updates) {
        String shortType = resolveShortType(type);
        return getSystemToken().flatMap(token -> 
            apiClient.updateZone(shortType, id, updates, token)
                .doOnSuccess(v -> log.info("🔄 Zone {} mise à jour", id))
        );
    }

    @Override
    public Mono<Void> deleteRemoteZone(String type, UUID zoneId) {
        String shortType = resolveShortType(type);
        return getSystemToken().flatMap(token -> apiClient.deleteZone(shortType, zoneId, token));
    }

   @Override
@SuppressWarnings("unchecked")
public Mono<List<Map<String, Object>>> listRemoteZones(String category) {
    return getSystemToken().flatMap(token -> {
        Mono<Object> response;
        if ("circles".equalsIgnoreCase(category)) response = apiClient.getCircles(token);
        else if ("polygons".equalsIgnoreCase(category)) response = apiClient.getPolygons(token);
        else response = apiClient.getAllZones(token);

        return response.map(res -> {
            if (res instanceof List) {
                return (List<Map<String, Object>>) res;
            } else if (res instanceof Map) {
                // Au cas où Kamga renvoie un objet avec une propriété "content" ou "zones"
                Map<String, Object> map = (Map<String, Object>) res;
                if (map.containsKey("content")) return (List<Map<String, Object>>) map.get("content");
                return List.of(map);
            }
            return Collections.emptyList();
        });
    });
}

    @Override
    public Mono<Map<String, Object>> getRemoteZoneDetails(String type, UUID id) {
        return getSystemToken().flatMap(token -> apiClient.getZoneById(resolveShortType(type), id, token));
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
            if (!ring.isEmpty() && !ring.get(0).equals(ring.get(ring.size()-1))) ring.add(ring.get(0));
            request.put("polygon", Map.of("type", "Polygon", "coordinates", List.of(ring)));
        }
        return request;
    }
}