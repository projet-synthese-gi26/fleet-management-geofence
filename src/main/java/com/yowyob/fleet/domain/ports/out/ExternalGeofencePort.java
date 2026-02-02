package com.yowyob.fleet.domain.ports.out;

import com.yowyob.fleet.domain.model.GeofenceZone;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ExternalGeofencePort {
    Mono<Void> synchronizeZone(GeofenceZone zone);
    Mono<Void> updateRemoteZone(String type, UUID id, Map<String, Object> updates);
    Mono<Void> deleteRemoteZone(String type, UUID zoneId);
    Mono<String> checkPointInZone(UUID zoneId, Double lat, Double lng);
    
    // Nouvelles méthodes de lecture
    Mono<List<Map<String, Object>>> listRemoteZones(String category); // all, circles, polygons
    Mono<Map<String, Object>> getRemoteZoneDetails(String type, UUID id);
    Mono<Map<String, Object>> fetchRemoteAlerts(int page, int size);
    
    Mono<String> getSystemToken();
}