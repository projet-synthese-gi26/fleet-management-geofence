package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import com.yowyob.fleet.domain.model.Vehicle;
import com.yowyob.fleet.domain.model.VehicleParameters;
import com.yowyob.fleet.domain.ports.in.ManageVehicleUseCase;
import com.yowyob.fleet.domain.ports.out.AuthPort;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders; // Import
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.service.annotation.PatchExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "06. Vehicles", description = "Gestion complète du parc automobile")
@SecurityRequirement(name = "bearerAuth")
public class VehicleController {

    private final ManageVehicleUseCase vehicleUseCase;

    private UUID getUserId(Authentication auth) {
        return ((AuthPort.UserDetail) auth.getPrincipal()).id();
    }

    // Helper pour extraire le token propre (sans "Bearer " si besoin, mais l'adapter gère déjà l'ajout)
    // Ici on récupère le header brut
    private String getToken(String authHeader) {
        return authHeader; 
    }

    // --- CRÉATION ---

    @PostMapping("/fleets/{fleetId}/vehicles")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    @Operation(summary = "Ajouter un véhicule à une flotte")
    public Mono<Vehicle> createInFleet(
            @PathVariable UUID fleetId, 
            @Valid @RequestBody VehicleRegistrationRequest request,
            Authentication auth,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        return vehicleUseCase.createVehicle(fleetId, request, getUserId(auth), getToken(authHeader));
    }

    @PostMapping("/vehicles")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    @Operation(summary = "Créer un véhicule indépendant (Hors flotte)")
    public Mono<Vehicle> createIndependent(
            @Valid @RequestBody VehicleRegistrationRequest request,
            Authentication auth,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        return vehicleUseCase.createIndependentVehicle(request, getUserId(auth), getToken(authHeader));
    }

    // --- LECTURE ---

    @GetMapping("/vehicles/{vehicleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER', 'FLEET_DRIVER')")
    @Operation(summary = "Obtenir les détails complets (Locaux + Distants)")
    public Mono<Vehicle> getVehicle(
            @PathVariable UUID vehicleId,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        return vehicleUseCase.getVehicleDetails(vehicleId, getToken(authHeader));
    }

    @GetMapping("/vehicles")
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    @Operation(summary = "Lister tous mes véhicules (Indépendants et en flotte)")
    public Flux<Vehicle> getMyVehicles(Authentication auth) {
        return vehicleUseCase.getMyVehicles(getUserId(auth));
    }

    // --- MODIFICATION ---

    @PutMapping("/vehicles/{vehicleId}")
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    @Operation(summary = "Mise à jour complète (Distant)")
    public Mono<Vehicle> updateFull(
            @PathVariable UUID vehicleId, 
            @RequestBody VehicleRegistrationRequest request,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        return vehicleUseCase.updateVehicleInfo(vehicleId, request, getToken(authHeader));
    }

    @PatchExchange("/vehicles/{vehicleId}")
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    @Operation(summary = "Mise à jour partielle (ex: kilométrage)")
    public Mono<Vehicle> updatePartial(
            @PathVariable UUID vehicleId, 
            @RequestParam String brand, 
            @RequestParam String model,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        return vehicleUseCase.patchVehicleInfo(vehicleId, brand, model, getToken(authHeader));
    }

    @DeleteMapping("/vehicles/{vehicleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    public Mono<Void> delete(
            @PathVariable UUID vehicleId,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        return vehicleUseCase.removeVehicle(vehicleId, getToken(authHeader));
    }

    // --- GESTION DES DOCUMENTS & PHOTOS ---

    @PutMapping(value = "/vehicles/{id}/documents/serial", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload photo VIN")
    public Mono<Void> uploadVin(
            @PathVariable UUID id, 
            @RequestPart("file") FilePart file,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        return vehicleUseCase.uploadVinPhoto(id, file, getToken(authHeader));
    }

    @DeleteMapping("/vehicles/{id}/documents/serial")
    public Mono<Void> deleteVin(
            @PathVariable UUID id,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        return vehicleUseCase.deleteVinPhoto(id, getToken(authHeader));
    }

    @PutMapping(value = "/vehicles/{id}/documents/registration", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload photo Carte Grise")
    public Mono<Void> uploadReg(
            @PathVariable UUID id, 
            @RequestPart("file") FilePart file,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        return vehicleUseCase.uploadRegistrationPhoto(id, file, getToken(authHeader));
    }

    @DeleteMapping("/vehicles/{id}/documents/registration")
    public Mono<Void> deleteReg(
            @PathVariable UUID id,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        return vehicleUseCase.deleteRegistrationPhoto(id, getToken(authHeader));
    }

    @PostMapping(value = "/vehicles/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Ajouter une image d'illustration")
    public Mono<Void> addImage(
            @PathVariable UUID id, 
            @RequestPart("file") FilePart file,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        return vehicleUseCase.addIllustrationImage(id, file, getToken(authHeader));
    }

    @GetMapping("/vehicles/{id}/images")
    public Flux<String> getImages(
            @PathVariable UUID id,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        return vehicleUseCase.getIllustrationImages(id, getToken(authHeader));
    }

    @DeleteMapping("/vehicles/images/{imageId}")
    public Mono<Void> deleteImage(
            @PathVariable UUID imageId,
            @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        return vehicleUseCase.deleteIllustrationImage(imageId, getToken(authHeader));
    }

    // --- PARAMÈTRES (Locaux uniquement, pas besoin de token externe) ---

    @PutMapping("/vehicles/{vehicleId}/financial-parameters")
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    public Mono<Void> updateFinancial(
            @PathVariable UUID vehicleId,
            @RequestBody VehicleParameters.Financial params
    ) {
        return vehicleUseCase.updateFinancialParameters(vehicleId, params);
    }

    @PutMapping("/vehicles/{vehicleId}/maintenance-parameters")
    @PreAuthorize("hasRole('FLEET_MANAGER')")
    public Mono<Void> updateMaintenance(
            @PathVariable UUID vehicleId,
            @RequestBody VehicleParameters.Maintenance params
    ) {
        return vehicleUseCase.updateMaintenanceParameters(vehicleId, params);
    }
}