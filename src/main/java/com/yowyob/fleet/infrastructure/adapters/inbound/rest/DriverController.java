package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import com.yowyob.fleet.domain.model.Driver;
import com.yowyob.fleet.domain.ports.in.ManageDriverUseCase;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.DriverRegistrationRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
@Tag(name = "Drivers", description = "Operations for Driver Management")
public class DriverController {

    private final ManageDriverUseCase driverUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new Driver", description = "Creates a remote Auth account and a local Fleet profile")
    public Mono<Driver> register(@Valid @RequestBody DriverRegistrationRequest request) {
        return driverUseCase.registerDriver(request);
    }

    @GetMapping
    @Operation(summary = "List drivers by fleet", description = "Retrieve all drivers belonging to a specific fleet")
    public Flux<Driver> listByFleet(
        @Parameter(
            name = "fleetId", 
            description = "UUID of the fleet", 
            required = true,
            in = ParameterIn.QUERY, 
            schema = @Schema(type = "string", format = "uuid")
        ) 
        @RequestParam UUID fleetId
    ) {
        return driverUseCase.getDriversByFleet(fleetId);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get driver details", description = "Retrieve local profile and vehicle assignment")
    public Mono<Driver> get(
        @Parameter(
            name = "userId",
            description = "Unique identifier (UUID) of the user/driver",
            in = ParameterIn.PATH,
            schema = @Schema(type = "string", format = "uuid")
        )
        @PathVariable UUID userId
    ) {
        return driverUseCase.getDriverById(userId);
    }

    @PostMapping("/{userId}/assign-vehicle")
    @Operation(summary = "Assign vehicle", description = "Link a vehicle to a driver")
    public Mono<Void> assign(@PathVariable UUID userId, @Valid @RequestBody VehicleAssignRequest req) {
        return driverUseCase.assignVehicle(userId, req.vehicleId());
    }

    @PostMapping("/{userId}/unassign-vehicle")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Unassign vehicle", description = "Remove current vehicle link from driver")
    public Mono<Void> unassign(@PathVariable UUID userId) {
        return driverUseCase.unassignVehicle(userId);
    }

    /**
     * DTO interne pour l'assignation de véhicule.
     */
    public record VehicleAssignRequest(
        @io.swagger.v3.oas.annotations.media.Schema(description = "UUID of the vehicle to assign", required = true)
        UUID vehicleId
    ) {}
}