package com.yowyob.fleet.application.service;

import com.yowyob.fleet.domain.model.Driver;
import com.yowyob.fleet.domain.ports.in.AuthUseCase;
import com.yowyob.fleet.domain.ports.in.ManageDriverUseCase;
import com.yowyob.fleet.domain.ports.out.AuthPort;
import com.yowyob.fleet.domain.ports.out.DriverPersistencePort;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.DriverRegistrationRequest;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.FleetR2dbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverService implements ManageDriverUseCase {

    private final DriverPersistencePort driverPersistencePort;
    private final AuthPort authPort;
    private final FleetR2dbcRepository fleetRepository; // Pour vérifier la propriété

    private static final String SERVICE_NAME = "FLEET_MANAGEMENT";

    // --- 1. CRÉATION COMPLÈTE ---
    @Override
    public Mono<Driver> registerDriver(DriverRegistrationRequest request, UUID managerId) {
        return checkFleetOwnership(request.fleetId(), managerId)
            .then(Mono.defer(() -> {
                AuthUseCase.RegisterCommand command = new AuthUseCase.RegisterCommand(
                    request.username(), request.password(), request.email(), request.phone(),
                    request.firstName(), request.lastName(), List.of("FLEET_DRIVER"), null
                );
                return authPort.registerInRemote(command);
            }))
            .flatMap(authRes -> {
                Driver localDriver = new Driver(
                    authRes.user().id(),
                    request.fleetId(),
                    request.licenceNumber(),
                    "ACTIVE",
                    null,
                    ""
                );
                return driverPersistencePort.save(localDriver);
            });
    }

    // --- 2. RECRUTEMENT (Filtrage manuel) ---
    @Override
    public Mono<Void> recruitDriver(UUID fleetId, String identifier, UUID managerId, String token) {
        return checkFleetOwnership(fleetId, managerId)
            .thenMany(authPort.getUsersByService(SERVICE_NAME, token)) // Récupère tout
            .filter(u -> isMatch(u, identifier)) // Filtre localement
            .filter(u -> u.roles().contains("FLEET_DRIVER")) // Vérifie que c'est un chauffeur
            .next() // Prend le premier
            .switchIfEmpty(Mono.error(new RuntimeException("Aucun chauffeur trouvé avec cet identifiant : " + identifier)))
            .flatMap(user -> driverPersistencePort.updateFleetAssignment(user.id(), fleetId));
    }

    private boolean isMatch(AuthPort.UserDetail user, String id) {
        return id.equalsIgnoreCase(user.email()) || 
               id.equalsIgnoreCase(user.username()) || 
               id.equals(user.phone());
    }

    // --- 3. LECTURE SÉCURISÉE ---
    @Override
    public Flux<Driver> getDrivers(UUID fleetId, UUID requesterId, boolean isAdmin) {
        if (isAdmin) {
            return fleetId != null ? driverPersistencePort.findAllByFleetId(fleetId) : driverPersistencePort.findAll();
        }
        // Manager : Doit fournir un fleetId ET posséder la flotte
        if (fleetId == null) {
            // TODO: On pourrait implémenter "toutes mes flottes" ici, mais on commence simple
            return Flux.error(new IllegalArgumentException("fleetId obligatoire pour les managers"));
        }
        return checkFleetOwnership(fleetId, requesterId)
                .thenMany(driverPersistencePort.findAllByFleetId(fleetId));
    }

    @Override
    public Mono<Driver> getDriverById(UUID userId) {
        // TODO: Ajouter sécurité (Un manager ne devrait voir que SES chauffeurs)
        return driverPersistencePort.findById(userId);
    }

    // --- 4. GESTION LIENS ---
    @Override
    public Mono<Void> removeDriverFromFleet(UUID fleetId, UUID driverId, UUID requesterId) {
        return checkFleetOwnership(fleetId, requesterId)
                .then(driverPersistencePort.updateFleetAssignment(driverId, null));
    }

    @Override
    public Mono<Void> assignVehicle(UUID userId, UUID vehicleId, UUID requesterId) {
        // TODO: Vérifier que le véhicule appartient bien au manager (via la flotte)
        return driverPersistencePort.updateVehicleAssignment(userId, vehicleId);
    }

    @Override
    public Mono<Void> unassignVehicle(UUID userId, UUID requesterId) {
        return driverPersistencePort.updateVehicleAssignment(userId, null);
    }

    // --- HELPER ---
    private Mono<Void> checkFleetOwnership(UUID fleetId, UUID managerId) {
        return fleetRepository.existsByIdAndManagerId(fleetId, managerId)
                .flatMap(exists -> {
                    if (!exists) return Mono.error(new AccessDeniedException("Cette flotte ne vous appartient pas."));
                    return Mono.empty();
                });
    }
}