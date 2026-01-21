package com.yowyob.fleet.application.service;

import com.yowyob.fleet.domain.model.Driver;
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
        
        return authPort.register(
            request.username(), request.password(), request.email(),
            request.phone(), request.firstName(), request.lastName(),
            List.of("FLEET_DRIVER")
        ).flatMap(authRes -> {
            log.info("Compte crée (ID: {}), création du profil local dans la flotte {}", authRes.user().id(), request.fleetId());
            
            Driver driverToSave = new Driver(
                authRes.user().id(),
                request.fleetId(),
                request.licenceNumber(),
                true,
                null,
                request.photoUrl()
            );
            
            return driverPersistencePort.save(driverToSave);
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