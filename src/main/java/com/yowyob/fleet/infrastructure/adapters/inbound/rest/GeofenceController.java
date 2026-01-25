package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import com.yowyob.fleet.domain.model.GeofenceZone;
import com.yowyob.fleet.domain.ports.in.ManageGeofenceUseCase;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.GeofenceEventEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/geofence")
@RequiredArgsConstructor
@Tag(name = "08. Geofencing", description = "Gestion des zones et historique des alertes")
public class GeofenceController {

    private final ManageGeofenceUseCase geofenceService;

    @PostMapping("/zones")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer une nouvelle zone (Polygone ou Cercle)")
    public Mono<GeofenceZone> create(@RequestBody GeofenceZone zone) {
        return geofenceService.createZone(zone);
    }

    @GetMapping("/zones/{id}")
    @Operation(summary = "Détails d'une zone avec ses points")
    public Mono<GeofenceZone> getById(@PathVariable UUID id) {
        return geofenceService.getZoneDetails(id);
    }

    @GetMapping("/fleets/{fleetId}/zones")
    @Operation(summary = "Lister toutes les zones d'une flotte")
    public Flux<GeofenceZone> listByFleet(@PathVariable UUID fleetId) {
        return geofenceService.getZonesByFleet(fleetId);
    }

    @PutMapping("/zones/{id}")
    @Operation(summary = "Mettre à jour une zone")
    public Mono<GeofenceZone> update(@PathVariable UUID id, @RequestBody GeofenceZone zone) {
        return geofenceService.updateZone(id, zone);
    }

    @DeleteMapping("/zones/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une zone")
    public Mono<Void> delete(@PathVariable UUID id) {
        return geofenceService.deleteZone(id);
    }

    @GetMapping("/events")
    @Operation(summary = "Consulter le log des alertes (ENTRY/EXIT) avec filtres")
    public Flux<GeofenceEventEntity> getEvents(
            @RequestParam(required = false) UUID vehicleId,
            @RequestParam(required = false) UUID zoneId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return geofenceService.getEvents(vehicleId, zoneId, type, date);
    }
}