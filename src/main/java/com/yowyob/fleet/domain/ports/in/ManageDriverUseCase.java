package com.yowyob.fleet.domain.ports.in;

import com.yowyob.fleet.domain.model.Driver;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.DriverRegistrationRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface ManageDriverUseCase {
    // Création complète (Manager)
    Mono<Driver> registerDriver(DriverRegistrationRequest request, UUID managerId);
    
    // Recrutement existant (Manager)
    Mono<Void> recruitDriver(UUID fleetId, String identifier, UUID managerId, String token);
    
    // Lecture
    Mono<Driver> getDriverById(UUID userId);
    Flux<Driver> getDrivers(UUID fleetId, UUID requesterId, boolean isAdmin);
    
    // Gestion Véhicule
    Mono<Void> assignVehicle(UUID userId, UUID vehicleId, UUID requesterId);
    Mono<Void> unassignVehicle(UUID userId, UUID requesterId);
    
    // Retrait de la flotte (Manager)
    Mono<Void> removeDriverFromFleet(UUID fleetId, UUID driverId, UUID requesterId);
}