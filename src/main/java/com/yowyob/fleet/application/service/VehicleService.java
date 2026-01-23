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
        // Aggrégation Parallèle : Local + Externe
        return Mono.zip(
                localPersistencePort.getLocalDataById(vehicleId),
                externalVehiclePort.getExternalVehicleInfo(vehicleId)
        ).map(tuple -> {
            Vehicle local = tuple.getT1();
            Vehicle remote = tuple.getT2();

            // On fusionne les données techniques distantes avec l'exploitation locale
            return new Vehicle(
                    vehicleId,
                    local.fleetId(),
                    local.currentDriverId(),
                    local.vehicleTypeId(),
                    remote.licensePlate(),
                    remote.brand(),
                    remote.model(),
                    remote.manufacturingYear(),
                    remote.type(),
                    remote.color(),
                    local.status(),
                    local.photoUrl(),
                    local.financialParameters(),
                    local.maintenanceParameters(),
                    null // Operational params gérés à part
            );
        });
    }

    @Override
    public Mono<Vehicle> addVehicleToFleet(Vehicle vehicle) {
        return externalVehiclePort.getExternalVehicleInfo(vehicle.id())
                .flatMap(remote -> localPersistencePort.saveLocalData(vehicle));
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
        return localPersistencePort.deleteLocalData(vehicleId);
    }
}