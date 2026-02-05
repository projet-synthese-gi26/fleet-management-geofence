package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import com.yowyob.fleet.application.service.GeofenceService;
import com.yowyob.fleet.domain.model.GeofencePoint;
import com.yowyob.fleet.domain.model.GeofenceZone;
import com.yowyob.fleet.domain.ports.in.ManageGeofenceUseCase;
import com.yowyob.fleet.domain.ports.out.AuthPort;
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

import org.springframework.security.core.Authentication;
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

    private UUID getUserId(Authentication auth) {
        return ((AuthPort.UserDetail) auth.getPrincipal()).id();
    }

    @PostMapping("/zones")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer une nouvelle géofence", description = "Envoie la zone au moteur spatial. Pour un POLYGON, la première et la dernière coordonnée doivent être identiques.")
    @PreAuthorize("hasAnyRole('FLEET_MANAGER', 'FLEET_ADMIN')")
    public Mono<GeofenceZone> create(@Valid @RequestBody GeofenceZoneDTORequest request, Authentication auth) {

        // Conversion manuelle du DTO vers le Domaine GeofenceZone
        List<GeofencePoint> vertices = List.of();
        if (request.polygon() != null && !request.polygon().coordinates().isEmpty()) {
            // On extrait le premier ring (index 0) du polygone
            vertices = request.polygon().coordinates().get(0).stream()
                    .map(coord -> new GeofencePoint(null, coord.get(1), coord.get(0), null)) // Index 1=Lat, 0=Lng
                    .toList();
        }

        // Extract center point for circle zones
        Double centerLatitude = null;
        Double centerLongitude = null;
        if (request.center() != null && !request.center().coordinates().isEmpty()) {
            centerLongitude = request.center().coordinates().get(0);
            centerLatitude = request.center().coordinates().get(1);
        }

        // Create zone with the fleetManagerId as the fleetId (zones belong to fleet
        // managers)
        GeofenceZone domainZone = new GeofenceZone(
                UUID.randomUUID(),
                null, // Use fleetManagerId as fleetId since zones are managed at fleet manager level
                request.title(),
                request.description(),
                request.type(),
                centerLatitude,
                centerLongitude,
                request.radius(),
                request.isTemporalEnabled(),
                request.startTime(),
                request.endTime(),
                null,
                request.isConditionalEnabled(),
                null, null, null,
                true, // isActive
                null, null,
                vertices);

        // Passer fleetManagerId au service
        return geofenceService.createZone(domainZone, getUserId(auth));
    }

    @GetMapping("/circles")
    public Flux<Map<String, Object>> listCircles(Authentication auth) {
        return geofenceService.getZonesByManager(getUserId(auth), "circles");
    }

    @GetMapping("/polygons")
    public Flux<Map<String, Object>> listPolygons(Authentication auth) {
        return geofenceService.getZonesByManager(getUserId(auth), "polygons");
    }

    @GetMapping("/fleet/{fleetId}")
    @Operation(summary = "Lister les zones d'une flotte spécifique")
    public Flux<Map<String, Object>> listByFleet(@PathVariable UUID fleetId, Authentication auth) {
        return geofenceService.getZonesByFleet(getUserId(auth), fleetId);
    }

    @GetMapping("/{type}/{id}")
    @Operation(summary = "Récupérer une géofence par son détail")
    public Mono<Map<String, Object>> getById(@PathVariable String type, @PathVariable UUID id) {
        return geofenceService.getExternalZoneDetails(type, id);
    }

    @GetMapping("/all")
    @Operation(summary = "Lister TOUTES les zones du moteur (Admin)")
    @PreAuthorize("hasRole('FLEET_ADMIN')")
    public Mono<List<Map<String, Object>>> getAllZonesAdmin() {
        return geofenceService.getAllExternalZones("all")
                .collectList(); // Transforme le Flux en Mono<List> pour un JSON propre
        // .doOnNext(list -> log.info("✅ Envoi de {} zones vers Swagger", list.size()));
    }

    @PutMapping("/{type}/{id}")
    @Operation(summary = "Modifier une géofence")
    public Mono<Void> update(@PathVariable String type, @PathVariable UUID id,
            @RequestBody Map<String, Object> updates) {
        return geofenceService.updateRemoteZone(type, id, updates);
    }

    @DeleteMapping("/{type}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une géofence")
    public Mono<Void> delete(@PathVariable String type, @PathVariable UUID zoneId, @PathVariable UUID managerId) {
        return geofenceService.deleteZone(zoneId, type, managerId);
    }

    @GetMapping("/alerts")
    @Operation(summary = "Récupérer toutes mes alertes")
    public Mono<Map<String, Object>> getAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return geofenceService.getExternalAlerts(page, size);
    }

    @PatchMapping("/{id}/assign-fleet/{fleetId}")
    @Operation(summary = "Assigner une zone à une flotte")
    public Mono<Void> assignToFleet(@PathVariable UUID id, @PathVariable UUID fleetId, Authentication auth) {
        return ((GeofenceService) geofenceService).assignZoneToFleet(id, fleetId, getUserId(auth));
    }
    // src/main/java/com/yowyob/fleet/infrastructure/adapters/inbound/rest/GeofenceController.java

    // src/main/java/com/yowyob/fleet/infrastructure/adapters/inbound.rest/GeofenceController.java

    @GetMapping
    @Operation(summary = "Récupérer uniquement MES géofences")
    public Mono<List<Map<String, Object>>> listMyZones(org.springframework.security.core.Authentication auth) {
        // Extraction de l'ID du manager depuis le token
        UUID managerId = ((com.yowyob.fleet.domain.ports.out.AuthPort.UserDetail) auth.getPrincipal()).id();

        return geofenceService.getAllExternalZones(managerId, "all")
                .collectList();
                // .doOnNext(list -> log.info("✅ {} zones envoyées au manager {}", list.size(), managerId));
    }
}