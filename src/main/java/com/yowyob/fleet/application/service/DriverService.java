package com.yowyob.fleet.application.service;

import com.yowyob.fleet.domain.model.Driver;
import com.yowyob.fleet.domain.model.Vehicle;
import com.yowyob.fleet.domain.ports.in.AuthUseCase;
import com.yowyob.fleet.domain.ports.in.ManageDriverUseCase;
import com.yowyob.fleet.domain.ports.out.AuthPort;
import com.yowyob.fleet.domain.ports.out.DriverPersistencePort;
import com.yowyob.fleet.domain.ports.out.VehiclePersistencePort;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.DriverRegistrationRequest;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.NotificationType;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.FleetR2dbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.yowyob.fleet.domain.ports.out.SendNotificationPort;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.SendNotificationRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverService implements ManageDriverUseCase {

    private final DriverPersistencePort driverPersistencePort;
    private final VehiclePersistencePort vehiclePersistencePort; // Nécessaire pour Smart Swap
    private final AuthPort authPort;
    private final FleetR2dbcRepository fleetRepository; 
    private final SendNotificationPort notificationPort; // Injection du port de notif

    private static final String SERVICE_NAME = "FLEET_MANAGEMENT";

    // --- 1. CRÉATION COMPLÈTE ---
    @Override
    public Mono<Driver> registerDriver(UUID fleetId, DriverRegistrationRequest request, UUID managerId) {
        return checkFleetOwnership(fleetId, managerId)
            .then(Mono.defer(() -> {
                // Pas de photo ici
                AuthUseCase.RegisterCommand command = new AuthUseCase.RegisterCommand(
                    request.username(), request.password(), request.email(), request.phone(),
                    request.firstName(), request.lastName(), List.of("FLEET_DRIVER"), null
                );
                return authPort.registerInRemote(command);
            }))
            .flatMap(authRes -> {
                Driver localDriver = new Driver(
                    authRes.user().id(),
                    fleetId,
                    request.licenceNumber(),
                    "ACTIVE",
                    null,
                    null // La photo est gérée par le driver lui-même
                );
                return driverPersistencePort.save(localDriver);
            });
    }

    // --- 2. RECRUTEMENT ---
    @Override
    public Mono<Void> recruitDriver(UUID fleetId, String identifier, UUID managerId, String token) {
        return checkFleetOwnership(fleetId, managerId)
            .thenMany(authPort.getUsersByService(SERVICE_NAME, token)) 
            .filter(u -> isMatch(u, identifier)) 
            .filter(u -> u.roles().contains("FLEET_DRIVER")) 
            .next() 
            .switchIfEmpty(Mono.error(new RuntimeException("Aucun chauffeur trouvé avec cet identifiant : " + identifier)))
            .flatMap(user -> driverPersistencePort.updateFleetAssignment(user.id(), fleetId));
    }

    private boolean isMatch(AuthPort.UserDetail user, String id) {
        return id.equalsIgnoreCase(user.email()) || 
               id.equalsIgnoreCase(user.username()) || 
               id.equals(user.phone());
    }

    // --- 3. LECTURE ---
    @Override
    public Flux<Driver> getDrivers(UUID fleetId, UUID requesterId, boolean isAdmin) {
        if (isAdmin) {
            return fleetId != null ? driverPersistencePort.findAllByFleetId(fleetId) : driverPersistencePort.findAll();
        }
        if (fleetId == null) {
            return Flux.error(new IllegalArgumentException("fleetId obligatoire pour les managers"));
        }
        return checkFleetOwnership(fleetId, requesterId)
                .thenMany(driverPersistencePort.findAllByFleetId(fleetId));
    }

    @Override
    public Mono<Driver> getDriverById(UUID userId) {
        return driverPersistencePort.findById(userId);
    }

    // --- 4. GESTION LIENS ---
    @Override
    public Mono<Void> removeDriverFromFleet(UUID fleetId, UUID driverId, UUID requesterId) {
        return checkFleetOwnership(fleetId, requesterId)
                .then(unassignVehicle(driverId, requesterId)) // On libère le véhicule avant de virer le chauffeur
                .then(driverPersistencePort.updateFleetAssignment(driverId, null));
    }

    /**
     * SMART SWAP : Assignation Intelligente
     */
    @Override
    @Transactional
    public Mono<Void> assignVehicle(UUID driverId, UUID targetVehicleId, UUID requesterId) {
        // 1. On récupère le véhicule cible pour vérifier sa flotte (Sécurité)
        return vehiclePersistencePort.getLocalDataById(targetVehicleId)
            .switchIfEmpty(Mono.error(new RuntimeException("Véhicule introuvable")))
            .flatMap(vehicle -> checkFleetOwnership(vehicle.fleetId(), requesterId).thenReturn(vehicle))
            .flatMap(targetVehicle -> {
                // 2. Gestion Chauffeur Cible : Avait-il un autre véhicule ?
                Mono<Void> clearOldVehicleOfDriver = driverPersistencePort.findById(driverId)
                    .flatMap(driver -> {
                        if (driver.assignedVehicleId() != null && !driver.assignedVehicleId().equals(targetVehicleId)) {
                            // On détache l'ancien véhicule du chauffeur
                            return updateVehicleLink(driver.assignedVehicleId(), null);
                        }
                        return Mono.empty();
                    });

                // 3. Gestion Véhicule Cible : Avait-il un autre chauffeur ?
                Mono<Void> clearOldDriverOfVehicle = driverPersistencePort.findByAssignedVehicleId(targetVehicleId)
                    .flatMap(oldDriver -> {
                        if (!oldDriver.userId().equals(driverId)) {
                            // On détache l'ancien chauffeur de ce véhicule
                            return driverPersistencePort.updateVehicleAssignment(oldDriver.userId(), null);
                        }
                        return Mono.empty();
                    });

                // 4. Exécution Atomique
                return clearOldVehicleOfDriver
                        .then(clearOldDriverOfVehicle)
                        .then(updateVehicleLink(targetVehicleId, driverId)) // Met à jour le véhicule
                        .then(driverPersistencePort.updateVehicleAssignment(driverId, targetVehicleId)); // Met à jour le driver
            });
            
    }
     private Mono<Void> sendAssignmentNotification(UUID driverId, UUID vehicleId) {
        return authPort.getUserById(driverId, "SYSTEM")
            .flatMap((AuthPort.UserDetail user) -> { // Type explicite pour aider le compilateur
                SendNotificationRequest request = SendNotificationRequest.builder()
                        .notificationType(NotificationType.PUSH)
                        .templateId(3) 
                        .to(List.of(user.email())) 
                        .data(Map.of(
                                "driverName", user.firstName() != null ? user.firstName() : user.username(),
                                "vehicleId", vehicleId.toString()
                        ))
                        .build();
                
                return notificationPort.sendNotification(request);
            })
            .then(); // On transforme le Mono<Boolean> de notificationPort en Mono<Void>
    }

    
    @Override
    @Transactional
    public Mono<Void> unassignVehicle(UUID driverId, UUID requesterId) {
        return driverPersistencePort.findById(driverId)
            .flatMap(driver -> {
                if (driver.assignedVehicleId() == null) return Mono.empty();
                
                // On met à jour le véhicule (driver = null)
                return updateVehicleLink(driver.assignedVehicleId(), null)
                        .then(driverPersistencePort.updateVehicleAssignment(driverId, null));
            });
    }

    // Helper pour mettre à jour la FK côté Véhicule
    private Mono<Void> updateVehicleLink(UUID vehicleId, UUID driverId) {
        return vehiclePersistencePort.getLocalDataById(vehicleId)
            .flatMap(v -> {
                Vehicle updated = new Vehicle(
                    v.id(), v.fleetId(), driverId, v.vehicleTypeId(), 
                    v.licensePlate(), v.brand(), v.model(), v.manufacturingYear(), v.type(), v.color(), 
                    v.status(), v.photoUrl(), v.financialParameters(), v.maintenanceParameters(), null
                );
                return vehiclePersistencePort.saveLocalData(updated);
            }).then();
    }

    // Helper Sécurité
    private Mono<Void> checkFleetOwnership(UUID fleetId, UUID managerId) {
        return fleetRepository.existsByIdAndManagerId(fleetId, managerId)
                .flatMap(exists -> {
                    if (!exists) return Mono.error(new AccessDeniedException("Cette flotte ne vous appartient pas."));
                    return Mono.empty();
                });
    }
}