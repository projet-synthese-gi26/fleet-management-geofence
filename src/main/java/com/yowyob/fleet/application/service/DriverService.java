package com.yowyob.fleet.application.service;

import com.yowyob.fleet.domain.model.Driver;
import com.yowyob.fleet.domain.ports.in.AuthUseCase;
import com.yowyob.fleet.domain.ports.in.ManageDriverUseCase;
import com.yowyob.fleet.domain.ports.out.AuthPort;
import com.yowyob.fleet.domain.ports.out.DriverPersistencePort;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.DriverRegistrationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public Mono<Driver> registerDriver(DriverRegistrationRequest request) {
        log.info("Enrôlement chauffeur : Appel Auth Service pour {}", request.username());
        
        // 1. Préparation de la commande avec le rôle spécifique
        AuthUseCase.RegisterCommand command = new AuthUseCase.RegisterCommand(
            request.username(),
            request.password(),
            request.email(),
            request.phone(),
            request.firstName(),
            request.lastName(),
            List.of("FLEET_DRIVER"), // On utilise le rôle standardisé
            null // Pas de photo via cet endpoint spécifique
        );

        // 2. Appel au service d'authentification distant
        return authPort.registerInRemote(command)
            .flatMap(authRes -> {
                log.info("Étape 2 : Création du profil chauffeur local pour UUID {}", authRes.user().id());
                
                // 3. Création du modèle de domaine Driver
                Driver localDriver = new Driver(
                    authRes.user().id(), // UUID TraMaSys
                    request.licenceNumber(),
                    true, // Actif par défaut
                    null  // Aucun véhicule au départ
                );
                
                // 4. Sauvegarde en base de données locale
                return driverPersistencePort.save(localDriver);
            });
    }

    @Override
    public Mono<Driver> getDriverById(UUID userId) {
        return driverPersistencePort.findById(userId);
    }

    @Override
    public Flux<Driver> getDriversByFleet(UUID fleetId) {
        return driverPersistencePort.findAllByFleetId(fleetId);
    }

    @Override
    public Mono<Void> assignVehicle(UUID userId, UUID vehicleId) {
        // Optionnel : Ajouter une logique de vérification (ex: véhicule libre ?)
        return driverPersistencePort.updateVehicleAssignment(userId, vehicleId);
    }

    @Override
    public Mono<Void> unassignVehicle(UUID userId) {
        return driverPersistencePort.updateVehicleAssignment(userId, null);
    }
}