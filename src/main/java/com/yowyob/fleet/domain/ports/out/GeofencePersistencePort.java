package com.yowyob.fleet.domain.ports.out;

import com.yowyob.fleet.domain.model.GeofenceZone;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.GeofenceEventEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDate;
import java.util.UUID;

public interface GeofencePersistencePort {
    Mono<GeofenceZone> saveZone(GeofenceZone zone);
    Mono<GeofenceZone> findById(UUID id); // Ajouté ou vérifié
    Flux<GeofenceZone> findByFleetId(UUID fleetId);
    Mono<Void> deleteById(UUID zoneId);
    Mono<Void> saveEvent(UUID vehicleId, UUID zoneId, String type);
    
    // Ajout de la méthode de filtrage pour le service
    Flux<GeofenceEventEntity> findEventsWithFilters(UUID vehicleId, UUID zoneId, String type, LocalDate date);
}