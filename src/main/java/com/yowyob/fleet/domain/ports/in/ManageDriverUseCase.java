package com.yowyob.fleet.domain.ports.in;

import com.yowyob.fleet.domain.model.Driver;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.DriverRegistrationRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface ManageDriverUseCase {
    Mono<Driver> registerDriver(DriverRegistrationRequest request);
    Mono<Driver> getDriverById(UUID userId);
    Flux<Driver> getDriversByFleet(UUID fleetId);
    Mono<Void> assignVehicle(UUID userId, UUID vehicleId);
    Mono<Void> unassignVehicle(UUID userId);
}