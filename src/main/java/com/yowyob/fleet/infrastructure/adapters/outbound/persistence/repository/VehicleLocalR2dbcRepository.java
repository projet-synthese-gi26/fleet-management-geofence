package com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository;

import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.VehicleLocalEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import java.util.UUID;

@Repository
public interface VehicleLocalR2dbcRepository extends ReactiveCrudRepository<VehicleLocalEntity, UUID> {
    
    /**
     * Finds all vehicle IDs associated with a specific fleet.
     */
    Flux<VehicleLocalEntity> findByFleetId(UUID fleetId);

    /**
     * Finds all vehicles assigned to a specific driver.
     */
    Flux<VehicleLocalEntity> findByDriverId(UUID driverId);
}