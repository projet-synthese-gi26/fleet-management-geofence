package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import com.yowyob.fleet.domain.model.Driver;
import com.yowyob.fleet.domain.ports.in.ManageDriverUseCase;
import com.yowyob.fleet.domain.ports.out.AuthPort;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.DriverRegistrationRequest;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.RecruitDriverRequest;

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
@Tag(name = "05. Drivers", description = "Gestion des chauffeurs (Création & Recrutement)")
@SecurityRequirement(name = "bearerAuth")
public class DriverController {

    private final ManageDriverUseCase driverUseCase;

    // Helper Auth
    private AuthPort.UserDetail getUser(Authentication auth) {
        return (AuthPort.UserDetail) auth.getPrincipal();
    }
    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_FLEET_ADMIN"));
    }
    private String getToken(String header) { return header.substring(7); }

    // 1. CRÉATION DIRECTE (Manager)
    @PostMapping("/drivers")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    @Operation(summary = "Créer un nouveau Chauffeur", description = "Crée le compte Auth + Profil local + Lien Flotte.")
    public Mono<Driver> register(
            @Valid @RequestBody DriverRegistrationRequest request,
            Authentication auth
    ) {
        return driverUseCase.registerDriver(request, getUser(auth).id());
    }

    // 2. RECRUTEMENT (Manager)
    @PostMapping("/fleets/{fleetId}/drivers")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    @Operation(summary = "Recruter un Chauffeur existant", description = "Recherche par email/username/tel et ajoute à la flotte.")
    public Mono<Void> recruit(
            @PathVariable UUID fleetId,
            @Valid @RequestBody RecruitDriverRequest request,
            Authentication auth,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        return driverUseCase.recruitDriver(fleetId, request.identifier(), getUser(auth).id(), getToken(authHeader));
    }

    // 3. LISTING (Admin/Manager)
    @GetMapping("/drivers")
    @Operation(summary = "Lister les chauffeurs", description = "Admin: Tout. Manager: Requis param 'fleetId'.")
    public Flux<Driver> list(
            @RequestParam(required = false) UUID fleetId,
            Authentication auth
    ) {
        return driverUseCase.getDrivers(fleetId, getUser(auth).id(), isAdmin(auth));
    }

    // 4. RETRAIT (Manager)
    @DeleteMapping("/fleets/{fleetId}/drivers/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    @Operation(summary = "Renvoyer un chauffeur", description = "Retire le chauffeur de la flotte (ne supprime pas le compte).")
    public Mono<Void> remove(
            @PathVariable UUID fleetId,
            @PathVariable UUID userId,
            Authentication auth
    ) {
        return driverUseCase.removeDriverFromFleet(fleetId, userId, getUser(auth).id());
    }

    // 5. DÉTAIL
    @GetMapping("/drivers/{userId}")
    public Mono<Driver> get(@PathVariable UUID userId) {
        return driverUseCase.getDriverById(userId);
    }

    // 6. ASSIGNATION VÉHICULE
    @PostMapping("/drivers/{userId}/assign-vehicle")
    public Mono<Void> assign(@PathVariable UUID userId, @RequestBody VehicleAssignRequest req, Authentication auth) {
        return driverUseCase.assignVehicle(userId, req.vehicleId(), getUser(auth).id());
    }

    @PostMapping("/drivers/{userId}/unassign-vehicle")
    public Mono<Void> unassign(@PathVariable UUID userId, Authentication auth) {
        return driverUseCase.unassignVehicle(userId, getUser(auth).id());
    }

    public record VehicleAssignRequest(UUID vehicleId) {}
}