package com.yowyob.fleet.application.service;

import com.yowyob.fleet.domain.model.GeofenceZone;
import com.yowyob.fleet.domain.ports.in.ManageGeofenceUseCase;
import com.yowyob.fleet.domain.ports.out.ExternalGeofencePort;
import com.yowyob.fleet.domain.ports.out.GeofencePersistencePort;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.GeofenceEventEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GeofenceService implements ManageGeofenceUseCase {

    private final GeofencePersistencePort persistencePort;
    private final ExternalGeofencePort externalPort;

    @Override
    public Mono<GeofenceZone> createZone(GeofenceZone zone) {
        return persistencePort.saveZone(zone);
    }

    @Override
    public Flux<GeofenceZone> getZonesByFleet(UUID fleetId) {
        return persistencePort.findByFleetId(fleetId);
    }

    @Override
    public Mono<Void> deleteZone(UUID zoneId) {
        return persistencePort.deleteById(zoneId);
    }

    @Override
    public Mono<GeofenceZone> getZoneDetails(UUID zoneId) {
        return persistencePort.findById(zoneId)
                .switchIfEmpty(Mono.error(new RuntimeException("Zone Geofence non trouvée : " + zoneId)));
    }

    @Override
    public Mono<GeofenceZone> updateZone(UUID zoneId, GeofenceZone zone) {
        return persistencePort.findById(zoneId)
                .flatMap(existingZone -> {
                    // On crée une copie du record avec l'ID correct pour garantir l'update
                    GeofenceZone updatedZone = new GeofenceZone(
                            zoneId,
                            zone.fleetId(),
                            zone.name(),
                            zone.description(),
                            zone.type(),
                            zone.radius(),
                            zone.surfaceArea(),
                            zone.perimeter(),
                            zone.vertices()
                    );
                    return persistencePort.saveZone(updatedZone);
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Impossible de mettre à jour : Zone inexistante")));
    }

    @Override
    public Flux<GeofenceEventEntity> getEvents(UUID vehicleId, UUID zoneId, String type, LocalDate date) {
        return persistencePort.findEventsWithFilters(vehicleId, zoneId, type, date);
    }

    @Override
    public Mono<Void> processVehicleLocation(UUID vehicleId, Double lat, Double lng) {
        // Cette méthode sera le coeur du moteur de détection au prochain jalon
        return Mono.empty(); 
    }
}