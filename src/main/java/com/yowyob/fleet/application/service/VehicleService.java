package com.yowyob.fleet.application.service;

import com.yowyob.fleet.domain.model.Vehicle;
import com.yowyob.fleet.domain.model.VehicleParameters;
import com.yowyob.fleet.domain.ports.in.ManageVehicleUseCase;
import com.yowyob.fleet.domain.ports.out.ExternalVehiclePort;
import com.yowyob.fleet.domain.ports.out.VehiclePersistencePort;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.VehicleRegistrationRequest; // Ajout temporaire pour accès facile aux champs
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.FleetR2dbcRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleService implements ManageVehicleUseCase {

    private final VehiclePersistencePort localPersistencePort;
    private final ExternalVehiclePort externalVehiclePort;
    private final FleetR2dbcRepository fleetRepository;

    @Override
    public Mono<Vehicle> getVehicleDetails(UUID vehicleId) {
        return Mono.zip(
                localPersistencePort.getLocalDataById(vehicleId),
                externalVehiclePort.getExternalVehicleInfo(vehicleId)
        ).map(tuple -> {
            Vehicle local = tuple.getT1();
            Vehicle remote = tuple.getT2();

            // Fusion : Données techniques distantes + Données d'exploitation locales
            return new Vehicle(
                    vehicleId,
                    local.fleetId(),
                    local.currentDriverId(),
                    local.vehicleTypeId(),
                    remote.licensePlate(),
                    remote.brand(),
                    remote.model(), // Peut être null selon réponse distante
                    remote.manufacturingYear(),
                    remote.type(),
                    remote.color(),
                    local.status(),
                    local.photoUrl() != null ? local.photoUrl() : remote.photoUrl(), // Priorité local ou remote ?
                    local.financialParameters(),
                    local.maintenanceParameters(),
                    null
            );
        });
    }

    /**
     * CRÉATION COMPLÈTE : Distant -> Local
     */
    @Override
    @Transactional
    public Mono<Vehicle> createVehicle(UUID fleetId, VehicleRegistrationRequest req) {
        log.info("Création véhicule Distant pour plaque: {}", req.licensePlate());
        
        // 1. Appel au service distant (Création simplifiée)
        return externalVehiclePort.createRemoteVehicle(req)
            .flatMap(remoteVehicle -> {
                log.info("Véhicule distant créé avec ID: {}", remoteVehicle.id());
                
                // 2. Création de l'objet Local avec l'ID reçu
                Vehicle localShell = new Vehicle(
                    remoteVehicle.id(), // ID imposé par le distant
                    fleetId,
                    null, // Pas de driver au début
                    req.vehicleTypeId(), // Type local (ex: Poids lourd)
                    remoteVehicle.licensePlate(),
                    remoteVehicle.brand(),
                    remoteVehicle.model(),
                    null, null, null, // Données techniques non stockées en local
                    "AVAILABLE",
                    req.photoUrl(),
                    null, null, null
                );

                // 3. Sauvegarde Locale
                return localPersistencePort.saveLocalData(localShell)
                        // 4. On retourne la fusion pour confirmation immédiate
                        .map(savedLocal -> new Vehicle(
                            savedLocal.id(), savedLocal.fleetId(), null, savedLocal.vehicleTypeId(),
                            remoteVehicle.licensePlate(), remoteVehicle.brand(), remoteVehicle.model(),
                            null, null, null,
                            savedLocal.status(), savedLocal.photoUrl(),
                            savedLocal.financialParameters(), savedLocal.maintenanceParameters(), null
                        ));
            });
    }

    @Override
    public Mono<Void> updateFinancialParameters(UUID vehicleId, VehicleParameters.Financial params) {
        return localPersistencePort.getLocalDataById(vehicleId)
                .flatMap(v -> localPersistencePort.saveLocalData(new Vehicle(
                        vehicleId, v.fleetId(), v.currentDriverId(), v.vehicleTypeId(),
                        null, null, null, null, null, null, v.status(), v.photoUrl(),
                        params, v.maintenanceParameters(), null
                )))
                .then();
    }

    @Override
    public Mono<Void> updateMaintenanceParameters(UUID vehicleId, VehicleParameters.Maintenance params) {
        return localPersistencePort.getLocalDataById(vehicleId)
                .flatMap(v -> localPersistencePort.saveLocalData(new Vehicle(
                        vehicleId, v.fleetId(), v.currentDriverId(), v.vehicleTypeId(),
                        null, null, null, null, null, null, v.status(), v.photoUrl(),
                        v.financialParameters(), params, null
                )))
                .then();
    }

    @Override
    public Mono<Void> removeVehicleFromFleet(UUID vehicleId) {
        log.info("Suppression véhicule {}: Distant puis Local", vehicleId);
        // 1. Suppression Distante
        return externalVehiclePort.deleteRemoteVehicle(vehicleId)
                // 2. Suppression Locale (même si distant échoue ? Non, on veut la cohérence)
                // En fait, si distant échoue (404), on peut quand même nettoyer localement.
                .onErrorResume(e -> {
                    log.warn("Erreur suppression distante (ou déjà supprimé): {}", e.getMessage());
                    return Mono.empty();
                })
                .then(localPersistencePort.deleteLocalData(vehicleId));
    }
    
    // Méthode héritée de l'interface précédente, gardée pour compatibilité si besoin
    @Override
    public Mono<Vehicle> addVehicleToFleet(Vehicle vehicle) {
        return Mono.error(new UnsupportedOperationException("Utiliser createVehicle avec DTO"));
    }

     public Flux<Vehicle> getVehiclesForManager(UUID managerId) {
        return fleetRepository.findAllByManagerId(managerId)
                // On transforme Flux<FleetEntity> en Flux<Vehicle>
                .<Vehicle>flatMap(fleet -> localPersistencePort.findByFleetId(fleet.getId()))
                // On enrichit chaque véhicule avec les données distantes
                .<Vehicle>flatMap(vLocal -> this.getVehicleDetails(vLocal.id()));
    }
}