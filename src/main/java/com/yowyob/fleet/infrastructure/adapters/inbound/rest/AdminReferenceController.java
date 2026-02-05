package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import com.yowyob.fleet.application.service.VehicleTypeService;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.VehicleTypeRequest;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.VehicleTypeEntity;
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
@RequestMapping("/api/v1/admin/references")
@RequiredArgsConstructor
@Tag(name = "00. Super Admin", description = "Configuration du système")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('FLEET_ADMIN')")
public class AdminReferenceController {

    private final VehicleTypeService vehicleTypeService;

    @PostMapping("/vehicle-types")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Ajouter un type de véhicule")
    public Mono<VehicleTypeEntity> createType(@Valid @RequestBody VehicleTypeRequest request) {
        return vehicleTypeService.createType(request);
    }

    @PutMapping("/vehicle-types/{id}")
    @Operation(summary = "Modifier un type de véhicule")
    public Mono<VehicleTypeEntity> updateType(@PathVariable UUID id, @Valid @RequestBody VehicleTypeRequest request) {
        return vehicleTypeService.updateType(id, request);
    }

    @DeleteMapping("/vehicle-types/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un type", description = "Impossible si des véhicules utilisent ce type.")
    public Mono<Void> deleteType(@PathVariable UUID id) {
        return vehicleTypeService.deleteType(id);
    }
}