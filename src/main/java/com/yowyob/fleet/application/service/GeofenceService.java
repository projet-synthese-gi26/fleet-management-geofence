package com.yowyob.fleet.application.service;

import com.yowyob.fleet.domain.model.GeofenceZone;
import com.yowyob.fleet.domain.ports.in.ManageGeofenceUseCase;
import com.yowyob.fleet.domain.ports.out.ExternalGeofencePort;
import com.yowyob.fleet.domain.ports.out.GeofencePersistencePort;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.GeofenceEventEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeofenceService implements ManageGeofenceUseCase {

    private final GeofencePersistencePort localPersistence;
    private final ExternalGeofencePort externalApi;

    @Override
    @Transactional

    public Mono<GeofenceZone> createZone(GeofenceZone zone) {
        return externalApi.synchronizeZone(zone)
                .thenReturn(zone);
    }

    @Override
    public Flux<GeofenceZone> getZonesByFleet(UUID fleetId) {
        return Flux.empty();
    }

    @Override
    public Mono<GeofenceZone> getZoneDetails(UUID zoneId) {
        return Mono.empty();
    }

    @Override
    public Flux<GeofenceEventEntity> getEvents(UUID vehicleId, UUID zoneId, String type, LocalDate date) {
        return localPersistence.findEventsWithFilters(vehicleId, zoneId, type, date);
    }

    @Override
    public Mono<GeofenceZone> updateZone(UUID zoneId, GeofenceZone zone) {
        // On force l'ID pour être sûr de mettre à jour la même zone
        GeofenceZone updatedZone = new GeofenceZone(
                zoneId, zone.fleetId(), zone.name(), zone.description(),
                zone.zoneType(), zone.centerLatitude(), zone.centerLongitude(),
                zone.radius(), zone.isTemporalEnabled(), zone.startTime(),
                zone.endTime(), zone.activeDays(), zone.isConditionalEnabled(),
                zone.maxSpeed(), zone.maxDwellTime(), zone.minDwellTime(),
                zone.isActive(), zone.surfaceArea(), zone.perimeter(), zone.vertices());

        return externalApi.synchronizeZone(updatedZone)
                .thenReturn(updatedZone);
    }

    @Override
    public Mono<Void> deleteZone(UUID zoneId, String type) {
        // On commence par la suppression distante, si elle réussit, on pourrait
        // supprimer en local
        // Note: Dans ton cas, on appelle l'API externe directement
        return externalApi.deleteRemoteZone(type, zoneId);
    }

    @Override
    public Flux<Map<String, Object>> getAllExternalZones(String category) {
        return externalApi.listRemoteZones(category)
                .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<Map<String, Object>> getExternalAlerts(int page, int size) {
        return externalApi.fetchRemoteAlerts(page, size);
    }

    @Override
    public Mono<String> checkPointInZone(UUID zoneId, Double lat, Double lng) {
        return externalApi.checkPointInZone(zoneId, lat, lng);
    }

    @Override
    public Mono<Map<String, Object>> getExternalZoneDetails(String type, UUID id) {
        log.info("🔍 Récupération des détails de la zone {} (type: {})", id, type);
        return externalApi.getRemoteZoneDetails(type, id);
    }

    @Override
    public Mono<Void> updateRemoteZone(String type, UUID id, Map<String, Object> updates) {
        log.info("🔄 Mise à jour distante de la zone {} (type: {})", id, type);
        return externalApi.updateRemoteZone(type, id, updates);
    }
    
}