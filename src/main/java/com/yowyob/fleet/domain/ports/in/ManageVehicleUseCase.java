package com.yowyob.fleet.domain.ports.in;

import com.yowyob.fleet.domain.model.Vehicle;
import com.yowyob.fleet.domain.model.VehicleParameters;

import reactor.core.publisher.Mono;
import java.util.UUID;

public interface ManageVehicleUseCase {
    /**
     * Aggregates local exploitation data with remote technical data.
     */
    Mono<Vehicle> getVehicleDetails(UUID vehicleId);

    /**
     * Adds a vehicle to a fleet after verifying its existence in the remote service.
     */
    Mono<Vehicle> addVehicleToFleet(Vehicle vehicle);

    Mono<Void> updateFinancialParameters(UUID vehicleId, VehicleParameters.Financial params);
    Mono<Void> updateMaintenanceParameters(UUID vehicleId, VehicleParameters.Maintenance params);
    Mono<Void> removeVehicleFromFleet(UUID vehicleId);
}