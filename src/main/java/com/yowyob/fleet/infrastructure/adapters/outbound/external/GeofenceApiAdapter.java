package com.yowyob.fleet.infrastructure.adapters.outbound.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.fleet.domain.model.GeofenceZone;
import com.yowyob.fleet.domain.model.Vehicle;
import com.yowyob.fleet.domain.ports.out.ExternalGeofencePort;
import com.yowyob.fleet.infrastructure.adapters.outbound.external.client.GeofenceApiClient;
import com.yowyob.fleet.infrastructure.adapters.outbound.external.client.GeofenceAuthClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
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
    public Mono synchronizeZone(GeofenceZone zone) {
        return getSystemToken().flatMap(token -> {
            Map<String, Object> request = buildGeofenceMap(zone);
            return apiClient.createZone(request, token)
                    .map(response -> {
                        Object idValue = response.get("id");
                        if (idValue == null) {
                            throw new RuntimeException("L'API externe n'a pas renvoyé d'ID");
                        }

                        String cleanId = idValue.toString().replace("\"", "").trim();

                        log.info("✅ Zone créée en externe. ID nettoyé : {}", cleanId);

                        try {
                            return UUID.fromString(cleanId);
                        } catch (IllegalArgumentException e) {
                            log.error("❌ Format d'UUID invalide reçu de l'API : {}", cleanId);
                            throw new RuntimeException("ID externe invalide : " + cleanId);
                        }
                        // ----------------------
                    });
        });
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
                            .map(node -> objectMapper.convertValue(node, new TypeReference<Map<String, Object>>() {
                            }))
                            .onErrorResume(e -> {
                                // On capture l'erreur réelle pour ne pas l'étouffer
                                if (e instanceof org.springframework.web.reactive.function.client.WebClientResponseException ex) {
                                    log.error("❌ Geofence Service Error: {} - Body: {}",
                                            ex.getStatusCode(), ex.getResponseBodyAsString());
                                    return Mono.error(
                                            new RuntimeException("Erreur externe: " + ex.getResponseBodyAsString()));
                                }
                                return Mono.error(e);
                            });
                });
    }

    @Override
    public Mono<List<Map<String, Object>>> listRemoteZones(String category) {
      // Appelle la version sans userId (récupère tout si admin, ou ses propres zones si token user)
        return getSystemToken().flatMap(token -> fetchZonesInternal(category, null, token));
    }

    // IMPLEMENTATION DE LA NOUVELLE METHODE
    @Override
    public Mono<List<Map<String, Object>>> getZonesByOwner(UUID ownerId, String category) {
        log.info("🔍 Récupération des zones pour le manager {} (via System Token)", ownerId);
        return getSystemToken().flatMap(token -> fetchZonesInternal(category, ownerId, token));
    }

    @Override
    public Mono<Map<String, Object>> fetchRemoteAlerts(int page, int size) {
        return getSystemToken().flatMap(token -> apiClient.getAlerts(page, size, token));
    }

    @Override
    public Mono<String> checkPointInZone(UUID zoneId, Double lat, Double lng) {
        return getSystemToken().flatMap(token -> apiClient.checkPoint(zoneId.toString(), lat, lng, token));
    }
    // Dans GeofenceApiAdapter.java

    private Map<String, Object> buildGeofenceMap(GeofenceZone zone) {
        Map<String, Object> request = new HashMap<>();

        // --- CORRECTION ICI ---
        // On n'ajoute l'ID au payload que s'il existe (cas du PUT).
        // Pour le POST, zone.id() est null, donc on ne met rien.
        if (zone.id() != null) {
            request.put("id", zone.id().toString());
        }
        // ----------------------

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
            // Vérification de sécurité sur les vertices pour éviter un autre NPE
            if (zone.vertices() != null && !zone.vertices().isEmpty()) {
                List<List<Double>> ring = new ArrayList<>(zone.vertices().stream()
                        .map(v -> Arrays.asList(v.longitude(), v.latitude())).toList());

                // Fermeture du polygone si nécessaire (le premier point doit être égal au
                // dernier)
                if (!ring.get(0).equals(ring.get(ring.size() - 1))) {
                    ring.add(ring.get(0));
                }
                request.put("polygon", Map.of("type", "Polygon", "coordinates", List.of(ring)));
            }
        }
        return request;
    }

    @Override
    public Flux<Map<String, Object>> getZonesByManager(UUID managerId, String category) {
        // On délègue à la méthode de listing interne qui gère déjà l'appel API et le
        // Token
        // Note: Le filtrage par managerId n'est pas encore géré par l'API externe, on
        // renvoie tout.
        return listRemoteZones(category)
                .flatMapMany(Flux::fromIterable);
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


    // Méthode helper pour éviter la duplication
    private Mono<List<Map<String, Object>>> fetchZonesInternal(String category, UUID userId, String token) {
        // Le type local doit correspondre au retour du Client (Mono<JsonNode>)
        Mono<JsonNode> response;

        if ("circles".equalsIgnoreCase(category)) {
            response = apiClient.getCircles(userId, token);
        } else if ("polygons".equalsIgnoreCase(category)) {
            response = apiClient.getPolygons(userId, token);
        } else {
            response = apiClient.getAllZones(userId, token);
        }

        return response.map(jsonNode -> {
            List<Map<String, Object>> result = new ArrayList<>();
            
            // Cas 1 : C'est un tableau JSON direct [...]
            if (jsonNode.isArray()) {
                jsonNode.forEach(node -> result.add(convertNodeToMap(node)));
            } 
            // Cas 2 : C'est un objet (Pagination ?) qui contient une liste "content"
            else if (jsonNode.has("content") && jsonNode.get("content").isArray()) {
                jsonNode.get("content").forEach(node -> result.add(convertNodeToMap(node)));
            } 
            // Cas 3 : C'est un objet unique (ou une erreur wrapped)
            else if (!jsonNode.isEmpty()) {
                result.add(convertNodeToMap(jsonNode));
            }
            
            return result;
        }).defaultIfEmpty(Collections.emptyList());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertNodeToMap(JsonNode node) {
        return objectMapper.convertValue(node, Map.class);
    }
// --- DANS GeofenceApiAdapter.java ---

    @Override
    public Mono<Void> registerVehicleAndAssignToZone(Vehicle vehicle, UUID zoneId, String zoneType) {
        return getSystemToken().flatMap(token -> {
            
            // 1. Préparation du Body pour l'API Geofence
            Map<String, Object> vehicleRequest = new HashMap<>();
            vehicleRequest.put("brand", vehicle.brand());
            vehicleRequest.put("model", vehicle.model());
            
            // ✅ CORRECTION ICI : On utilise la vraie plaque d'immatriculation
            vehicleRequest.put("licensePlate", vehicle.licensePlate()); 
            
            vehicleRequest.put("description", "Importé depuis Fleet Management");

            log.info("🚀 Sync Véhicule vers Geofence API : Plaque {}", vehicle.licensePlate());

            // 2. Appel Création
            return apiClient.createVehicle(vehicleRequest, token)
                .flatMap(responseNode -> {
                    // 3. Extraction de l'ID technique généré par l'API Geofence
                    // (On a toujours besoin de l'ID technique pour l'appel suivant, même si on lie par la plaque)
                    String remoteVehicleId = null;
                    
                    if (responseNode.has("id")) {
                        remoteVehicleId = responseNode.get("id").asText();
                    } else if (responseNode.has("_id")) {
                        remoteVehicleId = responseNode.get("_id").asText();
                    } else if (responseNode.has("data") && responseNode.get("data").has("id")) {
                        remoteVehicleId = responseNode.get("data").get("id").asText();
                    }

                    if (remoteVehicleId == null) {
                        // Cas robuste : Si le véhicule existe déjà (conflit sur la plaque), 
                        // il faudrait idéalement récupérer son ID existant.
                        // Pour l'instant, on lève une erreur explicite.
                        return Mono.error(new RuntimeException("Impossible de récupérer l'ID distant pour le véhicule : " + vehicle.licensePlate()));
                    }

                    log.info("✅ Véhicule créé/récupéré dans Geofence (Remote ID: {}). Assignation à la zone {}...", remoteVehicleId, zoneId);

                    // 4. Appel Assignation à la zone (utilise l'ID technique)
                    String shortType = resolveShortType(zoneType);
                    return apiClient.addVehicleToZone(remoteVehicleId, shortType, zoneId, token);
                })
                .doOnSuccess(v -> log.info("🎉 Véhicule {} assigné à la zone {} avec succès.", vehicle.licensePlate(), zoneId))
                .doOnError(e -> log.error("❌ Erreur lors de la synchro Geofence du véhicule {} : {}", vehicle.licensePlate(), e.getMessage()));
        });
    }


}