package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import com.yowyob.fleet.application.service.VehicleResourceService;
import com.yowyob.fleet.application.service.VehicleTypeService;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.VehicleTypeRequest;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.FuelTypeEntity;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.ManufacturerEntity;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.VehicleTypeEntity;
import com.yowyob.fleet.infrastructure.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/resources")
@RequiredArgsConstructor
@Tag(name = OpenApiConfig.TAG_ADMIN_RESOURCES, description = "Endpoints d'administration des ressources (types de véhicules, marques, carburants)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('FLEET_ADMIN', 'FLEET_SUPER_ADMIN')")
public class AdminResourceController {

    private final VehicleTypeService typeService;
    private final VehicleResourceService resourceService;

    // --- TYPES (Existant) ---
    @PostMapping("/vehicle-types")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<VehicleTypeEntity> createType(@RequestBody VehicleTypeRequest req) { return typeService.createType(req); }

    @GetMapping("/vehicle-types")
    public Flux<VehicleTypeEntity> listTypes() { return typeService.getAllTypes(); }

    // --- MARQUES (Nouveau) ---
    @PostMapping("/manufacturers")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Ajouter une marque (Admin)")
    public Mono<ManufacturerEntity> createMfr(@RequestBody ManufacturerEntity req) { return resourceService.createManufacturer(req); }

    @GetMapping("/manufacturers")
    public Flux<ManufacturerEntity> listMfrs() { return resourceService.getAllManufacturers(); }

    // --- CARBURANTS (Nouveau) ---
    @PostMapping("/fuel-types")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Ajouter un type de carburant (Admin)")
    public Mono<FuelTypeEntity> createFuel(@RequestBody FuelTypeEntity req) { return resourceService.createFuelType(req); }

    @GetMapping("/fuel-types")
    public Flux<FuelTypeEntity> listFuels() { return resourceService.getAllFuelTypes(); }
}