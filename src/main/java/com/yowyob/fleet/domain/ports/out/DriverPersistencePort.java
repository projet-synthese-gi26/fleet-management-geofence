package com.yowyob.fleet.domain.ports.out;

import com.yowyob.fleet.domain.model.Driver;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Port de sortie pour la gestion de la persistence des chauffeurs.
 */
public interface DriverPersistencePort {

    /**
     * Sauvegarde ou met à jour un chauffeur dans la base locale.
     */
    Mono<Driver> save(Driver driver);

    /**
     * Récupère un chauffeur par son identifiant utilisateur unique (UUID TraMaSys).
     */
    Mono<Driver> findById(UUID userId);

    /**
     * Récupère tous les chauffeurs rattachés à une flotte spécifique.
     */
    Flux<Driver> findAllByFleetId(UUID fleetId);

    /**
     * Met à jour l'assignation du véhicule pour un chauffeur.
     * @param userId Identifiant du chauffeur.
     * @param vehicleId Identifiant du véhicule (peut être null pour désassigner).
     */
    Mono<Void> updateVehicleAssignment(UUID userId, UUID vehicleId);
}