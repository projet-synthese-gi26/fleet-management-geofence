package com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository;

import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.VehicleLocalEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import java.util.UUID;

@Repository
public interface VehicleLocalR2dbcRepository extends ReactiveCrudRepository<VehicleLocalEntity, UUID> {

    Flux<VehicleLocalEntity> findByFleetId(UUID fleetId);
    Flux<VehicleLocalEntity> findByManagerId(UUID managerId); // Nouveau
    Flux<VehicleLocalEntity> findByStatus(String status);
    Flux<VehicleLocalEntity> findByCurrentDriverId(UUID currentDriverId);
}