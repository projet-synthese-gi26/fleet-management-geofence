package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import com.yowyob.fleet.domain.ports.in.ManageFleetManagerUseCase;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.FleetManagerResponse;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.UpdateManagerRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/managers")
@RequiredArgsConstructor
@Tag(name = "04. Fleet Managers", description = "Administration des entreprises (Réservé ADMIN)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('FLEET_ADMIN') or hasRole('ADMIN')") 
public class AdminManagerController {

    private final ManageFleetManagerUseCase manageFleetManagerUseCase;

    @GetMapping
    @Operation(summary = "Lister tous les managers", description = "Récupère la liste agrégée (Local + Auth).")
    public Flux<FleetManagerResponse> getAll(
        @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String token
    ) {
        return manageFleetManagerUseCase.getAllManagers(token);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détails d'un manager", description = "Infos complètes.")
    public Mono<FleetManagerResponse> getOne(
        @PathVariable UUID id,
        @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String token
    ) {
        return manageFleetManagerUseCase.getManagerDetails(id, token);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour l'entreprise", description = "Modifie uniquement le nom de la compagnie.")
    public Mono<Void> updateCompany(
        @PathVariable UUID id, 
        @Valid @RequestBody UpdateManagerRequest request
    ) {
        return manageFleetManagerUseCase.updateManagerCompany(id, request.companyName());
    }

    // @DeleteMapping("/{id}")
    // @ResponseStatus(HttpStatus.NO_CONTENT)
    // @Operation(summary = "Supprimer un manager", description = "Attention: Échoue si le service distant refuse la suppression (Sécurité).")
    // public Mono<Void> delete(
    //     @PathVariable UUID id,
    //     @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String token
    // ) {
    //     return manageFleetManagerUseCase.deleteManager(id, token);
    // }
}