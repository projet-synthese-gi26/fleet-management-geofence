package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import com.yowyob.fleet.domain.model.Driver;
import com.yowyob.fleet.domain.ports.in.ManageDriverUseCase;
import com.yowyob.fleet.domain.ports.out.AuthPort;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.DriverRegistrationRequest;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.RecruitDriverRequest;
import com.yowyob.fleet.infrastructure.config.OpenApiConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name =  OpenApiConfig.TAG_DRIVERS, description = "Gestion des chauffeurs (Création & Recrutement)")
@SecurityRequirement(name = "bearerAuth")
public class DriverController {

    private final ManageDriverUseCase driverUseCase;

    private AuthPort.UserDetail getUser(Authentication auth) {
        return (AuthPort.UserDetail) auth.getPrincipal();
    }
    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_FLEET_ADMIN"));
    }
    private String getToken(String header) { return header.substring(7); }

    @PostMapping("/fleets/{fleetId}/drivers/register")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    @Operation(summary = "Créer un nouveau Chauffeur", description = "Crée le compte Auth + Profil local (Sans photo).")
    public Mono<Driver> register(
            @PathVariable UUID fleetId,
            @Valid @RequestBody DriverRegistrationRequest request,
            Authentication auth
    ) {
        return driverUseCase.registerDriver(fleetId, request, getUser(auth).id());
    }

    @PostMapping("/fleets/{fleetId}/drivers/recruit")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    public Mono<Void> recruit(
            @PathVariable UUID fleetId,
            @Valid @RequestBody RecruitDriverRequest request,
            Authentication auth,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        return driverUseCase.recruitDriver(fleetId, request.identifier(), getUser(auth).id(), getToken(authHeader));
    }

    @GetMapping("/drivers")
    public Flux<Driver> list(
            @RequestParam(required = false) UUID fleetId,
            Authentication auth
    ) {
        return driverUseCase.getDrivers(fleetId, getUser(auth).id(), isAdmin(auth));
    }

    @DeleteMapping("/fleets/{fleetId}/drivers/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    public Mono<Void> remove(
            @PathVariable UUID fleetId,
            @PathVariable UUID userId,
            Authentication auth
    ) {
        return driverUseCase.removeDriverFromFleet(fleetId, userId, getUser(auth).id());
    }

    @GetMapping("/drivers/{userId}")
    public Mono<Driver> get(@PathVariable UUID userId) {
        return driverUseCase.getDriverById(userId);
    }

    @PostMapping("/drivers/{userId}/assign-vehicle")
    @Operation(summary = "Assigner un véhicule (Smart Swap)", description = "Assigne le véhicule et gère automatiquement le détachement.")
    public Mono<Void> assign(
            @PathVariable UUID userId, 
            @RequestBody VehicleAssignRequest req, 
            Authentication auth,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader // Ajout
    ) {
        return driverUseCase.assignVehicle(userId, req.vehicleId(), getUser(auth).id(), getToken(authHeader));
    }

    @PostMapping("/drivers/{userId}/unassign-vehicle")
    public Mono<Void> unassign(@PathVariable UUID userId, Authentication auth) {
        return driverUseCase.unassignVehicle(userId, getUser(auth).id());
    }

    public record VehicleAssignRequest(UUID vehicleId) {}
}