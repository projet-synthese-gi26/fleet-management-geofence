package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import com.yowyob.fleet.domain.model.Vehicle;
import com.yowyob.fleet.domain.model.VehicleParameters;
import com.yowyob.fleet.domain.ports.in.ManageVehicleUseCase;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Vehicles", description = "Vehicle Exploitation Operations")
public class VehicleController {

    private final ManageVehicleUseCase vehicleUseCase;

    @PostMapping("/fleets/{fleetId}/vehicles")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add an existing vehicle to a fleet")
    public Mono<Vehicle> addVehicle(@PathVariable UUID fleetId, @Valid @RequestBody VehicleRegistrationRequest request) {
        // Create a shell vehicle object to pass to service
        Vehicle vehicleToAdd = new Vehicle(request.vehicleId(), fleetId, null, null, null, null, null, null, null, null, null);
        return vehicleUseCase.addVehicleToFleet(vehicleToAdd);
    }

    @GetMapping("/vehicles/{vehicleId}")
    @Operation(summary = "Get full aggregated vehicle details")
    public Mono<Vehicle> getVehicle(@PathVariable UUID vehicleId) {
        return vehicleUseCase.getVehicleDetails(vehicleId);
    }

    @PutMapping("/vehicles/{vehicleId}/financial-parameters")
    @Operation(summary = "Update financial parameters")
    public Mono<Void> updateFinancial(@PathVariable UUID vehicleId, @RequestBody FinancialUpdateRequest req) {
        var domainParams = new VehicleParameters.Financial(
                req.insuranceNumber(), req.insuranceExpiryDate(), req.registrationDate(),
                req.purchaseDate(), req.depreciationRate(), req.costPerKm());
        return vehicleUseCase.updateFinancialParameters(vehicleId, domainParams);
    }

    @PutMapping("/vehicles/{vehicleId}/maintenance-parameters")
    @Operation(summary = "Update maintenance parameters")
    public Mono<Void> updateMaintenance(@PathVariable UUID vehicleId, @RequestBody MaintenanceUpdateRequest req) {
        var domainParams = new VehicleParameters.Maintenance(
                req.lastMaintenanceDate(), req.nextMaintenanceDue(), req.engineStatus(),
                req.batteryHealth(), req.maintenanceStatus());
        return vehicleUseCase.updateMaintenanceParameters(vehicleId, domainParams);
    }

    @DeleteMapping("/vehicles/{vehicleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove vehicle from fleet management")
    public Mono<Void> delete(@PathVariable UUID vehicleId) {
        return vehicleUseCase.removeVehicleFromFleet(vehicleId);
    }
}