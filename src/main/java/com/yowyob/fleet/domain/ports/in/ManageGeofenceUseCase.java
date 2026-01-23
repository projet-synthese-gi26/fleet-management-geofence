package com.yowyob.fleet.domain.ports.in;

import com.yowyob.fleet.domain.model.GeofenceZone;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.GeofenceEventEntity; // DTO ou Entity selon préférence
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDate;
import java.util.UUID;

public interface ManageGeofenceUseCase {
    Mono<GeofenceZone> createZone(GeofenceZone zone);
    Mono<Void> processVehicleLocation(UUID vehicleId, Double lat, Double lng);
    Mono<GeofenceZone> getZoneDetails(UUID zoneId);
    Flux<GeofenceZone> getZonesByFleet(UUID fleetId);
    Mono<GeofenceZone> updateZone(UUID zoneId, GeofenceZone zone);
    Mono<Void> deleteZone(UUID zoneId);
    
    // Historique filtré
    Flux<GeofenceEventEntity> getEvents(UUID vehicleId, UUID zoneId, String type, LocalDate date);
}