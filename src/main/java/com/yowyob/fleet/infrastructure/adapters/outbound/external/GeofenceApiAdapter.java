package com.yowyob.fleet.infrastructure.adapters.outbound.external;

import com.yowyob.fleet.domain.ports.out.ExternalGeofencePort;
import com.yowyob.fleet.infrastructure.adapters.outbound.external.client.GeofenceApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeofenceApiAdapter implements ExternalGeofencePort {

    // CORRECTION : On injecte l'interface Client et non le WebClient brut
    private final GeofenceApiClient geofenceApiClient;

    @Override
    public Mono<String> checkPointInZone(UUID zoneId, Double lat, Double lng) {
        return geofenceApiClient.isPointInZone(zoneId, lat, lng)
            .doOnNext(status -> log.debug("Statut Geofence pour zone {}: {}", zoneId, status))
            .onErrorResume(e -> {
                log.error("Erreur d'appel au service Geofence distant : {}", e.getMessage());
                // On retourne UNKNOWN ou OUTSIDE par sécurité en cas de panne du service de calcul
                return Mono.just("UNKNOWN"); 
            });
    }
}