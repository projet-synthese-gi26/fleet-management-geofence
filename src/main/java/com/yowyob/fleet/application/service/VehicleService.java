package com.yowyob.fleet.application.service;

import com.yowyob.fleet.domain.model.Vehicle;
import com.yowyob.fleet.domain.model.VehicleParameters;
import com.yowyob.fleet.domain.ports.in.ManageVehicleUseCase;
import com.yowyob.fleet.domain.ports.out.ExternalVehiclePort;
import com.yowyob.fleet.domain.ports.out.VehiclePersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleService implements ManageVehicleUseCase {

    private final VehiclePersistencePort localPersistencePort;
    private final ExternalVehiclePort externalVehiclePort;

    @Override
    public Mono<Vehicle> getVehicleDetails(UUID vehicleId) {
        return Mono.zip(
                localPersistencePort.getLocalDataById(vehicleId),
                externalVehiclePort.getExternalVehicleInfo(vehicleId)
        ).map(tuple -> {
            Vehicle local = tuple.getT1();
            Vehicle remote = tuple.getT2();

            return new Vehicle(
                    vehicleId,
                    local.fleetId(),
                    remote.licensePlate(),
                    remote.brand(),
                    remote.model(),
                    remote.manufacturingYear(),
                    remote.type(),
                    remote.color(),
                    local.financialParameters(),
                    local.maintenanceParameters(),
                    local.operationalParameters()
            );
        });
    }

    @Override
    public Mono<Vehicle> addVehicleToFleet(Vehicle vehicle) {
        return externalVehiclePort.getExternalVehicleInfo(vehicle.id())
                .switchIfEmpty(Mono.error(new RuntimeException("Vehicle not found in external registry")))
                .flatMap(remoteInfo -> localPersistencePort.saveLocalData(vehicle));
    }

    @Override
    public Mono<Void> updateFinancialParameters(UUID vehicleId, VehicleParameters.Financial params) {
        return localPersistencePort.getLocalDataById(vehicleId)
                .flatMap(existing -> {
                    Vehicle updatedVehicle = new Vehicle(
                            vehicleId, existing.fleetId(), null, null, null, null, null, null,
                            params, existing.maintenanceParameters(), null
                    );
                    return localPersistencePort.saveLocalData(updatedVehicle);
                })
                .then();
    }

    @Override
    public Mono<Void> updateMaintenanceParameters(UUID vehicleId, VehicleParameters.Maintenance params) {
        return localPersistencePort.getLocalDataById(vehicleId)
                .flatMap(existing -> {
                    Vehicle updatedVehicle = new Vehicle(
                            vehicleId, existing.fleetId(), null, null, null, null, null, null,
                            existing.financialParameters(), params, null
                    );
                    return localPersistencePort.saveLocalData(updatedVehicle);
                })
                .then();
    }

    @Override
    public Mono<Void> removeVehicleFromFleet(UUID vehicleId) {
        return localPersistencePort.deleteLocalData(vehicleId);
    }
}