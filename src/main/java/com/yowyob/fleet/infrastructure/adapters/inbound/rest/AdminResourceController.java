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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/resources/vehicle-types")
@RequiredArgsConstructor
@Tag(name = "06. Admin | Gestion des Ressources")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('FLEET_ADMIN', 'FLEET_SUPER_ADMIN')")
public class AdminResourceController {

    private final VehicleTypeService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un nouveau type de véhicule")
    public Mono<VehicleTypeEntity> create(@Valid @RequestBody VehicleTypeRequest req) {
        return service.createType(req);
    }

    @GetMapping
    @Operation(summary = "Lister les types de véhicules")
    public Flux<VehicleTypeEntity> list() {
        return service.getAllTypes();
    }
   
    @GetMapping("/lookup")
    @Operation(summary = "Lister les types de véhicules (Référence)", 
               description = "Public ou Admin : permet de connaître les IDs pour la création de véhicules.")
    @PreAuthorize("permitAll()") // On autorise tout le monde à voir les types pour l'UI
    public Flux<VehicleTypeEntity> getLookupTypes() {
        return service.getAllTypes();
    }
    @PutMapping("/{id}")
    @Operation(summary = "Modifier un type de véhicule")
    public Mono<VehicleTypeEntity> update(@PathVariable UUID id, @Valid @RequestBody VehicleTypeRequest req) {
        return service.updateType(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un type de véhicule")
    public Mono<Void> delete(@PathVariable UUID id) {
        return service.deleteType(id);
    }
}