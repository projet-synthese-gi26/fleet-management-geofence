package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import com.yowyob.fleet.domain.model.Vehicle;
import com.yowyob.fleet.domain.model.VehicleParameters;
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
    @PreAuthorize("hasAnyRole('FLEET_MANAGER', 'FLEET_ADMIN')")
    public Mono<Vehicle> createVehicle(
            @PathVariable UUID fleetId, 
            @Valid @RequestBody VehicleRegistrationRequest request
    ) {
        return vehicleUseCase.createVehicle(fleetId, request);
    }

    @GetMapping("/vehicles/{vehicleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER', 'FLEET_DRIVER')")
    public Mono<Vehicle> getVehicle(@PathVariable UUID vehicleId) {
        return vehicleUseCase.getVehicleDetails(vehicleId);
    }

    @DeleteMapping("/vehicles/{vehicleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    public Mono<Void> delete(@PathVariable UUID vehicleId) {
        return vehicleUseCase.removeVehicleFromFleet(vehicleId);
    }

    // --- NOUVEAUX ENDPOINTS ---

    @PutMapping("/vehicles/{vehicleId}/financial-parameters")
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    @Operation(summary = "Mettre à jour les infos financières (Assurance, Achat)")
    public Mono<Void> updateFinancial(
            @PathVariable UUID vehicleId,
            @RequestBody VehicleParameters.Financial params
    ) {
        return vehicleUseCase.updateFinancialParameters(vehicleId, params);
    }

    @PutMapping("/vehicles/{vehicleId}/maintenance-parameters")
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    @Operation(summary = "Mettre à jour les infos maintenance (Révision, État)")
    public Mono<Void> updateMaintenance(
            @PathVariable UUID vehicleId,
            @RequestBody VehicleParameters.Maintenance params
    ) {
        return vehicleUseCase.updateMaintenanceParameters(vehicleId, params);
    }
}