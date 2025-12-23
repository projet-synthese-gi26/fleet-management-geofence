package com.yowyob.fleet.domain.ports.out;

import com.yowyob.fleet.domain.model.Vehicle;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface VehiclePersistencePort {
    /**
     * Saves the local data of a vehicle (Pivot, Financial, and Maintenance).
     */
    Mono<Vehicle> saveLocalData(Vehicle vehicle);

    /**
     * Retrieves only the local data stored in our database for a vehicle.
     */
    Mono<Vehicle> getLocalDataById(UUID id);

    /**
     * Deletes all local records associated with a vehicle ID.
     */
    Mono<Void> deleteLocalData(UUID id);
}