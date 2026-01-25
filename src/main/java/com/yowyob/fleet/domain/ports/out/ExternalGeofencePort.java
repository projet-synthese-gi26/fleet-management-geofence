package com.yowyob.fleet.domain.ports.out;

import reactor.core.publisher.Mono;
import java.util.UUID;

public interface ExternalGeofencePort {
    // Appel au service externe pour le calcul géométrique
    Mono<String> checkPointInZone(UUID zoneId, Double lat, Double lng);
}