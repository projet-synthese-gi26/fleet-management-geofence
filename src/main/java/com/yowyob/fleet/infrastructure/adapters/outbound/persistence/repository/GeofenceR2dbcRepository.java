package com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository;

import com.yowyob.fleet.domain.model.GeofenceZone;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.GeofenceZoneEntity;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface GeofenceR2dbcRepository extends ReactiveCrudRepository<GeofenceZoneEntity, UUID> {
    // On cherche directement dans la table de liaison par manager_id
 @Query("SELECT z.* FROM fleet.geofence_zones z " +
           "INNER JOIN fleet.fleets f ON z.fleet_id = f.id " +
           "WHERE f.manager_id = :managerId")
    Flux<GeofenceZoneEntity> findByManagerId(UUID managerId);
     Flux<GeofenceZoneEntity> findAllByManagerIdAndFleetId(UUID managerId, UUID fleetId);

     Flux<GeofenceZoneEntity> findAllByFleetId(UUID fleetId);
     
}