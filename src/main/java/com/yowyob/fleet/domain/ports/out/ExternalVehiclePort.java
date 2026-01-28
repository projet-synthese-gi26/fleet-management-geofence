package com.yowyob.fleet.domain.ports.out;

import com.yowyob.fleet.domain.model.Vehicle;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.VehicleRegistrationRequest;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface ExternalVehiclePort {
    Mono<Vehicle> getExternalVehicleInfo(UUID vehicleId);
    Mono<Vehicle> createRemoteVehicle(VehicleRegistrationRequest request);
    Mono<Void> deleteRemoteVehicle(UUID vehicleId);
}