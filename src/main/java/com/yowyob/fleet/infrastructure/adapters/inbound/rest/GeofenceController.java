package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import com.yowyob.fleet.domain.model.GeofencePoint;
import com.yowyob.fleet.domain.model.GeofenceZone;
import com.yowyob.fleet.domain.ports.in.ManageGeofenceUseCase;
import com.yowyob.fleet.infrastructure.adapters.outbound.external.dto.GeofenceZoneDTORequest;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.GeofenceEventEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/geofence")
@RequiredArgsConstructor
@Tag(name = "08. Geofencing")
@SecurityRequirement(name = "bearerAuth")
public class GeofenceController {

    private final ManageGeofenceUseCase geofenceService;

@PostMapping("/zones")
@ResponseStatus(HttpStatus.CREATED)
@Operation(summary = "Créer une nouvelle géofence", 
           description = "Envoie la zone au moteur spatial. Pour un POLYGON, la première et la dernière coordonnée doivent être identiques.")
@PreAuthorize("hasAnyRole('FLEET_MANAGER', 'FLEET_ADMIN')")
public Mono<GeofenceZone> create(@Valid @RequestBody GeofenceZoneDTORequest request) {
    
    // Conversion manuelle du DTO vers le Domaine GeofenceZone
    List<GeofencePoint> vertices = List.of();
    if (request.polygon() != null && !request.polygon().coordinates().isEmpty()) {
        // On extrait le premier ring (index 0) du polygone
        vertices = request.polygon().coordinates().get(0).stream()
                .map(coord -> new GeofencePoint(null, coord.get(1), coord.get(0), null)) // Index 1=Lat, 0=Lng
                .toList();
    }

    GeofenceZone domainZone = new GeofenceZone(
            UUID.randomUUID(),
            null, // fleetId à gérer selon votre logique
            request.title(),
            request.description(),
            request.type(),
            null, null, null, // center/radius
            request.isTemporalEnabled(),
            request.startTime(),
            request.endTime(),
            null,
            request.isConditionalEnabled(),
            null, null, null,
            true, // isActive
            null, null,
            vertices // C'est ici que les points sont injectés !
    );

    // Passer fleetManagerId au service
    return geofenceService.createZoneWithFleetManager(domainZone, request.fleetManagerId());
}

    @GetMapping
    @Operation(summary = "Récupérer toutes mes géofences")
    public Flux<Map<String, Object>> listAll() {
        return geofenceService.getAllExternalZones("all");
    }

    @GetMapping("/by-fleet-manager/{fleetManagerId}")
    @Operation(summary = "Récupérer les zones d'un FleetManager via la liaison")
    public Flux<GeofenceZone> getZonesByFleetManager(@PathVariable UUID fleetManagerId) {
        return geofenceService.getZonesByFleetManager(fleetManagerId);
    }

    @GetMapping("/circles")
    @Operation(summary = "Récupérer mes zones circulaires")
    public Flux<Map<String, Object>> listCircles() {
        return geofenceService.getAllExternalZones("circles");
    }

    @GetMapping("/polygons")
    @Operation(summary = "Récupérer mes zones polygonales")
    public Flux<Map<String, Object>> listPolygons() {
        return geofenceService.getAllExternalZones("polygons");
    }

    @GetMapping("/{type}/{id}")
    @Operation(summary = "Récupérer une géofence (par type + id)")
    public Mono<Map<String, Object>> getById(@PathVariable String type, @PathVariable UUID id) {
        // Logique de lecture unitaire à ajouter dans le service si besoin
        return Mono.empty(); 
    }

    @PutMapping("/{type}/{id}")
    @Operation(summary = "Modifier une géofence")
    public Mono<Void> update(@PathVariable String type, @PathVariable UUID id, @RequestBody Map<String, Object> updates) {
        return geofenceService.updateRemoteZone(type, id, updates);
    }

    @DeleteMapping("/{type}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une géofence")
    public Mono<Void> delete(@PathVariable String type, @PathVariable UUID id) {
        return geofenceService.deleteZone(id, type);
    }

    @GetMapping("/alerts")
    @Operation(summary = "Récupérer toutes mes alertes")
    public Mono<Map<String, Object>> getAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return geofenceService.getExternalAlerts(page, size);
    }
}