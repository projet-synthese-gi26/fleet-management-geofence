package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import com.yowyob.fleet.domain.model.Vehicle;
import com.yowyob.fleet.domain.model.VehicleParameters;
import com.yowyob.fleet.domain.ports.in.ManageVehicleUseCase;
import com.yowyob.fleet.domain.ports.out.AuthPort;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "09. Vehicles", description = "Gestion unifiée du parc")
@SecurityRequirement(name = "bearerAuth")
public class VehicleController {

    private final ManageVehicleUseCase vehicleUseCase;

    /**
     * Helper pour extraire le token JWT de l'objet Authentication
     * (Placé ici par le JwtAuthenticationManager lors de la validation)
     */
    private String extractToken(Authentication auth) {
        return "Bearer " + auth.getCredentials().toString();
    }

    private UUID getUserId(Authentication auth) {
        return ((AuthPort.UserDetail) auth.getPrincipal()).id();
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_FLEET_ADMIN") || 
                               a.getAuthority().equals("ROLE_FLEET_SUPER_ADMIN"));
    }

    @PostMapping("/vehicles")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    @Operation(summary = "Créer un véhicule")
    public Mono<Vehicle> create(@Valid @RequestBody VehicleRequest request, Authentication auth) {
        return vehicleUseCase.createIndependentVehicle(request, getUserId(auth), extractToken(auth));
    }

    @GetMapping("/vehicles/{vehicleId}")
    @Operation(summary = "Détails complets d'un véhicule")
    public Mono<Vehicle> getVehicle(@PathVariable UUID vehicleId, Authentication auth) {
        return vehicleUseCase.getVehicleDetails(vehicleId, extractToken(auth));
    }

   // Dans VehicleController.java

    @GetMapping("/vehicles")
    @PreAuthorize("hasAnyRole('FLEET_MANAGER', 'FLEET_ADMIN', 'FLEET_SUPER_ADMIN')")
    @Operation(summary = "Lister les véhicules (Synchronisés)")
    public Flux<Vehicle> getVehicles(Authentication auth) {
        // On passe l'identifiant, le flag admin ET le token pour la synchro distante
        return vehicleUseCase.getVehicles(getUserId(auth), isAdmin(auth), extractToken(auth));
    }

    // on va debugger ceci plutard
    // @PutMapping("/vehicles/{vehicleId}")
    // @PreAuthorize("hasRole('FLEET_MANAGER')")
    // @Operation(summary = "Mise à jour complète")
    // public Mono<Vehicle> update(@PathVariable UUID vehicleId, @Valid @RequestBody VehicleRequest request, Authentication auth) {
    //     return vehicleUseCase.updateVehicleInfo(vehicleId, request, extractToken(auth));
    // }

    @PatchMapping("/vehicles/{vehicleId}")
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    @Operation(summary = "Mise à jour partielle")
    public Mono<Vehicle> patch(@PathVariable UUID vehicleId, @RequestBody Map<String, Object> updates, Authentication auth) {
        return vehicleUseCase.patchVehicleInfo(vehicleId, updates, extractToken(auth));
    }

 
    @PutMapping("/vehicles/{vehicleId}/financial-parameters")
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    @Operation(summary = "Mise à jour paramètres financiers")
    public Mono<Vehicle> updateFinancial(@PathVariable UUID vehicleId, @RequestBody VehicleParameters.Financial params, Authentication auth) {
        return vehicleUseCase.updateFinancialParameters(vehicleId, params, extractToken(auth));
    }

    @PutMapping("/vehicles/{vehicleId}/maintenance-parameters")
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    @Operation(summary = "Mise à jour paramètres maintenance")
    public Mono<Vehicle> updateMaintenance(@PathVariable UUID vehicleId, @RequestBody VehicleParameters.Maintenance params, Authentication auth) {
        return vehicleUseCase.updateMaintenanceParameters(vehicleId, params, extractToken(auth));
    }

    @DeleteMapping("/vehicles/{vehicleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un véhicule")
    public Mono<Void> delete(@PathVariable UUID vehicleId, Authentication auth) {
        return vehicleUseCase.removeVehicle(vehicleId, extractToken(auth));
    }
}