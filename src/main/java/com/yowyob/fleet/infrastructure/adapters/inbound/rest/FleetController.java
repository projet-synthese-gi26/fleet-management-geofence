package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import com.yowyob.fleet.domain.model.Fleet;
import com.yowyob.fleet.domain.ports.in.ManageFleetUseCase;
import com.yowyob.fleet.domain.ports.out.AuthPort;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.FleetRequest;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.FleetResponse;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.FleetStatsResponse; // Import
import com.yowyob.fleet.infrastructure.mappers.FleetMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fleets")
@RequiredArgsConstructor
@Tag(name = "10. Fleets", description = "Gestion des flottes (Sécurisé par Propriétaire)")
@SecurityRequirement(name = "bearerAuth")
public class FleetController {

    private final ManageFleetUseCase fleetUseCase;
    private final FleetMapper mapper;

    // Helper pour extraire l'utilisateur du token
    private AuthPort.UserDetail getUser(Authentication auth) {
        return (AuthPort.UserDetail) auth.getPrincipal();
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_FLEET_ADMIN"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('FLEET_MANAGER') or hasRole('ADMIN')") // Seul un manager crée sa flotte
    @Operation(summary = "Créer une flotte", description = "La flotte sera automatiquement liée au manager connecté.")
    public Mono<FleetResponse> create(
            @Valid @RequestBody FleetRequest request,
            Authentication auth) {
        Fleet domainObj = mapper.toDomain(request);
        return fleetUseCase.createFleet(domainObj, getUser(auth).id())
                .map(mapper::toResponse);
    }

    @GetMapping
    @Operation(summary = "Lister les flottes", description = "Admin : Tout voir / Manager : Voir ses flottes uniquement.")
    public Flux<FleetResponse> getAll(Authentication auth) {
        return fleetUseCase.getFleets(getUser(auth).id(), isAdmin(auth))
                .map(mapper::toResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détails d'une flotte")
    public Mono<FleetResponse> getById(@PathVariable UUID id, Authentication auth) {
        return fleetUseCase.getFleetById(id, getUser(auth).id(), isAdmin(auth))
                .map(mapper::toResponse);
    }

    // --- AJOUT TÂCHE 6.2 ---
    @GetMapping("/{id}/stats")
    @Operation(summary = "Statistiques de la flotte", description = "KPIs : Nombre de chauffeurs, km totaux, état des véhicules.")
    public Mono<FleetStatsResponse> getStats(@PathVariable UUID id, Authentication auth) {
        return fleetUseCase.getFleetStatistics(id, getUser(auth).id(), isAdmin(auth));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une flotte")
    public Mono<FleetResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody FleetRequest request,
            Authentication auth) {
        return fleetUseCase.updateFleet(id, mapper.toDomain(request), getUser(auth).id(), isAdmin(auth))
                .map(mapper::toResponse);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une flotte")
    public Mono<Void> delete(@PathVariable UUID id, Authentication auth) {
        return fleetUseCase.deleteFleet(id, getUser(auth).id(), isAdmin(auth));
    }

    // Fichier :
    // src/main/java/com/yowyob/fleet/infrastructure/adapters/inbound/rest/FleetController.java

    @GetMapping("/all")
    @PreAuthorize("hasRole('FLEET_ADMIN')")
    @Operation(summary = "Lister TOUTES les flottes du système (Supervision Admin)")
    public Flux<FleetResponse> getAllFleetsAdmin() {
        // On passe un ID null ou spécial pour dire "Tout"
        return fleetUseCase.getFleets(null, true)
                .map(mapper::toResponse);
    }
}