package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import com.yowyob.fleet.domain.model.Vehicle;
import com.yowyob.fleet.domain.model.VehicleParameters;
import com.yowyob.fleet.domain.ports.in.ManageVehicleUseCase;
import com.yowyob.fleet.domain.ports.out.AuthPort;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.VehicleRequest;
import com.yowyob.fleet.infrastructure.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
@SecurityRequirement(name = "bearerAuth")
public class VehicleController {

    private final ManageVehicleUseCase vehicleUseCase;

    /**
     * Helper pour extraire le token JWT
     */
    private String extractToken(Authentication auth) {
        return "Bearer " + auth.getCredentials().toString();
    }

    private UUID getUserId(Authentication auth) {
        return ((AuthPort.UserDetail) auth.getPrincipal()).id();
    }

    // ========================================================================
    // --- TAG 09a. VEHICLES | GESTION DU PARC [ACTEUR: FLEET MANAGER] ---
    // ========================================================================

    @Tag(name = OpenApiConfig.TAG_VHC_PARC)
    @PostMapping("/vehicles")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    @Operation(summary = "Créer un véhicule", description = "Enregistrement distant (Pynfi) et initialisation locale. Acteur: Manager.")
    public Mono<Vehicle> create(@Valid @RequestBody VehicleRequest request, Authentication auth) {
        return vehicleUseCase.createIndependentVehicle(request, getUserId(auth), extractToken(auth));
    }

    @Tag(name = OpenApiConfig.TAG_VHC_PARC)
    @GetMapping("/vehicles")
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    @Operation(summary = "Lister mes véhicules", description = "Récupère les véhicules gérés par le manager connecté. Acteur: Manager.")
    public Flux<Vehicle> getVehicles(Authentication auth) {
        return vehicleUseCase.getVehicles(getUserId(auth), false, extractToken(auth));
    }

    @Tag(name = OpenApiConfig.TAG_VHC_PARC)
    @GetMapping("/vehicles/{vehicleId}")
    @PreAuthorize("hasAnyRole('FLEET_MANAGER', 'FLEET_ADMIN')")
    @Operation(summary = "Détails complets d'un véhicule", description = "Agrégation Identité + Finance + Maintenance + Opérationnel. Acteur: Manager/Admin.")
    public Mono<Vehicle> getVehicle(@PathVariable UUID vehicleId, Authentication auth) {
        return vehicleUseCase.getVehicleDetails(vehicleId, extractToken(auth));
    }

    /* 
    // MÉTHODE EN ATTENTE DE DEBUG (Mise à jour complète)
    @Tag(name = OpenApiConfig.TAG_VHC_PARC)
    @PutMapping("/vehicles/{vehicleId}")
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    @Operation(summary = "Mise à jour complète (Debug)", description = "Mise à jour totale des infos techniques.")
    public Mono<Vehicle> update(@PathVariable UUID vehicleId, @Valid @RequestBody VehicleRequest request, Authentication auth) {
        return vehicleUseCase.updateVehicleInfo(vehicleId, request, extractToken(auth));
    }
    */

    @Tag(name = OpenApiConfig.TAG_VHC_PARC)
    @PatchMapping("/vehicles/{vehicleId}")
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    @Operation(summary = "Mise à jour partielle", description = "Modifier couleur, statut ou marque. Acteur: Manager.")
    public Mono<Vehicle> patch(@PathVariable UUID vehicleId, @RequestBody Map<String, Object> updates, Authentication auth) {
        return vehicleUseCase.patchVehicleInfo(vehicleId, updates, extractToken(auth));
    }

    @Tag(name = OpenApiConfig.TAG_VHC_PARC)
    @PutMapping("/vehicles/{vehicleId}/financial-parameters")
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    @Operation(summary = "Paramètres Financiers", description = "Mise à jour Assurance, Coût/KM, Achat. Acteur: Manager.")
    public Mono<Vehicle> updateFinancial(@PathVariable UUID vehicleId, @RequestBody VehicleParameters.Financial params, Authentication auth) {
        return vehicleUseCase.updateFinancialParameters(vehicleId, params, extractToken(auth));
    }

    @Tag(name = OpenApiConfig.TAG_VHC_PARC)
    @PutMapping("/vehicles/{vehicleId}/maintenance-parameters")
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    @Operation(summary = "Paramètres Maintenance", description = "Mise à jour État moteur, Batterie, Révisions. Acteur: Manager.")
    public Mono<Vehicle> updateMaintenance(@PathVariable UUID vehicleId, @RequestBody VehicleParameters.Maintenance params, Authentication auth) {
        return vehicleUseCase.updateMaintenanceParameters(vehicleId, params, extractToken(auth));
    }

    @Tag(name = OpenApiConfig.TAG_VHC_PARC)
    @DeleteMapping("/vehicles/{vehicleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    @Operation(summary = "Supprimer un véhicule", description = "Suppression physique distante et locale. Acteur: Manager.")
    public Mono<Void> delete(@PathVariable UUID vehicleId, Authentication auth) {
        return vehicleUseCase.removeVehicle(vehicleId, extractToken(auth));
    }

    // ========================================================================
    // --- TAG 09c. VEHICLES | OPÉRATIONNEL [ACTEUR: DRIVER / MANAGER] ---
    // ========================================================================

    @Tag(name = OpenApiConfig.TAG_VHC_OP)
    @GetMapping("/vehicles/{vehicleId}/operational")
    @PreAuthorize("hasAnyRole('FLEET_DRIVER', 'FLEET_MANAGER')")
    @Operation(summary = "Consulter la télémétrie", description = "Voir position, vitesse et niveau de fuel en temps réel. Acteur: Driver/Manager.")
    public Mono<VehicleParameters.Operational> getOp(@PathVariable UUID vehicleId) {
        return vehicleUseCase.getOperationalData(vehicleId);
    }

    @Tag(name = OpenApiConfig.TAG_VHC_OP)
    @PatchMapping("/vehicles/{vehicleId}/operational")
    @PreAuthorize("hasRole('FLEET_DRIVER')")
    @Operation(summary = "Mise à jour terrain", description = "Permet au chauffeur d'ajuster l'odomètre ou le niveau de carburant. Acteur: Driver.")
    public Mono<Void> updateOp(@PathVariable UUID vehicleId, @RequestBody Map<String, Object> updates) {
        return vehicleUseCase.updateOperationalData(vehicleId, updates);
    }

    // ========================================================================
    // --- TAG 09d. VEHICLES | RÉFÉRENTIELS [ACTEUR: PUBLIC / MANAGER] ---
    // ========================================================================

    @Tag(name = OpenApiConfig.TAG_VHC_LOOKUP)
    @GetMapping("/vehicles/lookup/{resource}")
    @Operation(summary = "Listes de référence", description = "Récupère les options valides (manufacturers, fuel-types, vehicle-types) de la DB locale. Acteur: Tous.")
    public Flux<Map<String, Object>> getLookup(@PathVariable String resource) {
        return vehicleUseCase.getLocalLookupData(resource);
    }
}