package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import com.yowyob.fleet.domain.model.Vehicle;
import com.yowyob.fleet.domain.ports.in.ManageVehicleUseCase;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "06. Vehicles", description = "Opérations sur les véhicules")
@SecurityRequirement(name = "bearerAuth")
public class VehicleController {

    private final ManageVehicleUseCase vehicleUseCase;

    @PostMapping("/fleets/{fleetId}/vehicles")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('FLEET_ADMIN') or hasRole('ADMIN')")
    @Operation(summary = "Ajouter un véhicule à une flotte (ADMIN)")
    public Mono<Vehicle> addVehicle(@PathVariable UUID fleetId, @Valid @RequestBody VehicleRegistrationRequest request) {
        Vehicle shell = new Vehicle(request.vehicleId(), fleetId, null, request.vehicleTypeId(), 
                                    null, null, null, null, null, null, "AVAILABLE", null, null, null, null);
        return vehicleUseCase.addVehicleToFleet(shell);
    }

    @GetMapping("/vehicles/{vehicleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @Operation(summary = "Détails complets aggrégés (Local + Remote)")
    public Mono<Vehicle> getVehicle(@PathVariable UUID vehicleId) {
        return vehicleUseCase.getVehicleDetails(vehicleId);
    }

    @DeleteMapping("/vehicles/{vehicleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')  or hasRole('ADMIN')")
    public Mono<Void> delete(@PathVariable UUID vehicleId) {
        return vehicleUseCase.removeVehicleFromFleet(vehicleId);
    }
}