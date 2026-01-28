package com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository;

import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.TripEntity; // Assure-toi que cette entité existe ou est créée
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Repository
public interface TripR2dbcRepository extends ReactiveCrudRepository<TripEntity, UUID> {
    
    // Trouver le trajet actif d'un chauffeur
    Mono<TripEntity> findByDriverIdAndStatus(UUID driverId, String status);
    
    // Trouver le trajet actif d'un véhicule (pour vérifier dispo)
    Mono<TripEntity> findByVehicleIdAndStatus(UUID vehicleId, String status);
}