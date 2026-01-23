package com.yowyob.fleet.infrastructure.adapters.outbound.persistence;

import com.yowyob.fleet.domain.model.GeofenceZone;
import com.yowyob.fleet.domain.model.GeofencePoint;
import com.yowyob.fleet.domain.ports.out.GeofencePersistencePort;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.GeofenceEventEntity;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.GeofenceZoneEntity;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.GeofenceEventR2dbcRepository;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.GeofenceR2dbcRepository;
import com.yowyob.fleet.infrastructure.mappers.GeofenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GeofencePersistenceAdapter implements GeofencePersistencePort {

    private final GeofenceR2dbcRepository zoneRepo;
    private final GeofenceEventR2dbcRepository eventRepo;
    private final GeofenceMapper mapper;
    private final DatabaseClient databaseClient; // Pour le filtrage dynamique complexe

    @Override
    public Mono<GeofenceZone> saveZone(GeofenceZone zone) {
        // Enregistrement de la zone puis des points (simplifié ici pour la structure)
        return zoneRepo.save(mapper.toEntity(zone))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<GeofenceZone> findById(UUID id) {
        return zoneRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public Flux<GeofenceZone> findByFleetId(UUID fleetId) {
        return zoneRepo.findByFleetId(fleetId).map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID zoneId) {
        return zoneRepo.deleteById(zoneId);
    }

    // Requête dynamique pour les événements
    public Flux<GeofenceEventEntity> findEventsWithFilters(UUID vehicleId, UUID zoneId, String type, LocalDate date) {
        String query = "SELECT * FROM fleet.geofence_events WHERE 1=1";
        if (vehicleId != null) query += " AND vehicle_id = '" + vehicleId + "'";
        if (zoneId != null) query += " AND zone_id = '" + zoneId + "'";
        if (type != null) query += " AND type = '" + type + "'";
        if (date != null) query += " AND DATE(timestamp) = '" + date + "'";
        
        return databaseClient.sql(query)
                .map((row, metadata) -> GeofenceEventEntity.builder()
                        .id(row.get("id", UUID.class))
                        .vehicleId(row.get("vehicle_id", UUID.class))
                        .zoneId(row.get("zone_id", UUID.class))
                        .type(row.get("type", String.class))
                        .timestamp(row.get("timestamp", java.time.Instant.class))
                        .build())
                .all();
    }

    @Override
    public Mono<Void> saveEvent(UUID vehicleId, UUID zoneId, String type) {
        // ... implémentation existante
        return Mono.empty();
    }
}