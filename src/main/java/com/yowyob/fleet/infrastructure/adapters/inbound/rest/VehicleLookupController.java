package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import com.yowyob.fleet.domain.ports.in.ManageVehicleUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/vehicles/lookup")
@RequiredArgsConstructor
@Tag(name = "09. Vehicles", description = "Gestion complète du parc automobile")
@SecurityRequirement(name = "bearerAuth")
public class VehicleLookupController {

    private final ManageVehicleUseCase vehicleUseCase;

    @GetMapping("/{resource}")
    @PreAuthorize("hasAnyRole('FLEET_MANAGER', 'FLEET_ADMIN')")
    @Operation(
        summary = "Récupérer des listes de référence (Proxy)", 
        description = "Ressources dispos: fuel-types, manufacturers, transmission-types, vehicle-makes, vehicle-models, vehicle-sizes, vehicle-types"
    )
    public Flux<Map<String, Object>> getReferenceList(
            @PathVariable String resource,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        return vehicleUseCase.getVehicleReferenceData(resource, authHeader);
    }
}