package com.yowyob.fleet.domain.ports.in;

import com.yowyob.fleet.domain.model.Vehicle;
import com.yowyob.fleet.domain.model.VehicleParameters;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.VehicleRegistrationRequest; // Dépendance DTO acceptée au niveau input port pour simplifier

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface ManageVehicleUseCase {
    Mono<Vehicle> getVehicleDetails(UUID vehicleId);
    
    // Nouvelle signature principale
    Mono<Vehicle> createVehicle(UUID fleetId, VehicleRegistrationRequest request);
    
    @Deprecated
    Mono<Vehicle> addVehicleToFleet(Vehicle vehicle);

    Mono<Void> updateFinancialParameters(UUID vehicleId, VehicleParameters.Financial params);
    Mono<Void> updateMaintenanceParameters(UUID vehicleId, VehicleParameters.Maintenance params);
    Mono<Void> removeVehicleFromFleet(UUID vehicleId);
    
}