package com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository;

import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.DriverEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface DriverR2dbcRepository extends ReactiveCrudRepository<DriverEntity, UUID> {

    /**
     * Recherche tous les chauffeurs appartenant à une flotte spécifique.
     * Spring Data génère automatiquement le SQL basé sur le nom du champ 'fleetId' dans DriverEntity.
     */
    Flux<DriverEntity> findByFleetId(UUID fleetId);

    /**
     * Recherche par statut (actif/inactif).
     */
    Flux<DriverEntity> findByStatus(String status);

    /**
     * Recherche par ID de véhicule assigné.
     */
    Mono<DriverEntity> findByAssignedVehicleId(UUID assignedVehicleId);
    // --- AJOUT TÂCHE 6.2 ---
    Mono<Long> countByFleetId(UUID fleetId);
}