package com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository;

import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.GeofenceZoneEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import java.util.UUID;

@Repository
public interface GeofenceR2dbcRepository extends ReactiveCrudRepository<GeofenceZoneEntity, UUID> {
    Flux<GeofenceZoneEntity> findByFleetId(UUID fleetId);
}