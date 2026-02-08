package com.yowyob.fleet.application.service;

import com.yowyob.fleet.domain.model.GeofenceZone;
import com.yowyob.fleet.domain.ports.in.ManageGeofenceUseCase;
import com.yowyob.fleet.domain.ports.out.ExternalGeofencePort;
import com.yowyob.fleet.domain.ports.out.GeofencePersistencePort;
import com.yowyob.fleet.domain.ports.out.VehiclePersistencePort;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.GeofenceEventEntity;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.GeofenceZoneEntity;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.FleetEntity;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.FleetR2dbcRepository;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.GeofenceR2dbcRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeofenceService implements ManageGeofenceUseCase {

    private final GeofencePersistencePort localPersistence;
    private final ExternalGeofencePort externalApi;
    private final GeofenceR2dbcRepository zoneRepo;
    private final VehiclePersistencePort vehiclePersistencePort;

    @Override
    @Transactional
    public Mono<GeofenceZone> createZone(GeofenceZone zone) {
        // 1. Appel API Externe
        return externalApi.synchronizeZone(zone)
                .flatMap(externalId -> {
                    // 2. On crée une nouvelle instance du modèle de domaine avec l'ID externe
                    GeofenceZone zoneWithExternalId = new GeofenceZone(
                            externalId, // <--- L'ID venant de Kamga
                            zone.fleetId(), zone.managerId(), zone.name(), zone.description(),
                            zone.zoneType(), zone.centerLatitude(), zone.centerLongitude(),
                            zone.radius(), zone.isTemporalEnabled(), zone.startTime(),
                            zone.endTime(), zone.activeDays(), zone.isConditionalEnabled(),
                            zone.maxSpeed(), zone.maxDwellTime(), zone.minDwellTime(),
                            zone.isActive(), zone.surfaceArea(), zone.perimeter(),
                            zone.vertices());

                    // 3. Sauvegarde en base locale
                    log.info("💾 Sauvegarde locale de la zone avec ID synchronisé: {}", externalId);
                    return localPersistence.saveZone(zoneWithExternalId);
                })
                .doOnError(e -> log.error("❌ Échec de la synchronisation Geofence: {}", e.getMessage()));
    }

    // Helpers
    private boolean isType(Map<String, Object> zoneData, String type) {
        Object t = zoneData.get("type");
        return t != null && t.toString().equalsIgnoreCase(type);
    }

    private String normalizeType(String type) {
        if (type == null)
            return "";
        if (type.toLowerCase().startsWith("c"))
            return "circle";
        if (type.toLowerCase().startsWith("p"))
            return "polygon";
        return type;
    }

    
    @Override
    public Mono<GeofenceZone> updateZone(UUID zoneId, GeofenceZone zone) {
        // Correction du constructeur : on ajoute zone.managerId() en 3ème position (21
        // paramètres au total)
        GeofenceZone updatedZone = new GeofenceZone(
                zoneId,
                zone.fleetId(),
                zone.managerId(), // <--- AJOUTÉ ICI
                zone.name(),
                zone.description(),
                zone.zoneType(),
                zone.centerLatitude(),
                zone.centerLongitude(),
                zone.radius(),
                zone.isTemporalEnabled(),
                zone.startTime(),
                zone.endTime(),
                zone.activeDays(),
                zone.isConditionalEnabled(),
                zone.maxSpeed(),
                zone.maxDwellTime(),
                zone.minDwellTime(),
                zone.isActive(),
                zone.surfaceArea(),
                zone.perimeter(),
                zone.vertices());

        return externalApi.synchronizeZone(updatedZone)
                .thenReturn(updatedZone);
    }

    @Override
    public Flux<GeofenceZone> getZonesByFleet(UUID fleetId) {
        // 1. Récupérer les IDs locaux liés à la flotte
        return localPersistence.findByFleetId(fleetId)
                // 2. Pour chaque ID, aller chercher les détails distants
                .flatMap(localLink -> externalApi.getRemoteZoneDetails("all", localLink.id()) // "all" ou type
                                                                                              // spécifique si stocké
                        .map(details -> mapRemoteToDomain(localLink.id(), fleetId, localLink.managerId(), details))
                        // Si une zone n'existe plus en distant, on l'ignore ou on nettoie
                        .onErrorResume(e -> Mono.empty()));
    }

    @Override
    public Mono<GeofenceZone> getZoneDetails(UUID zoneId) {
        return Mono.empty();
    }

    // Dans GeofenceService.java

    @Override
    public Flux<Map<String, Object>> getMyExternalZones(String category) {
        log.info("🔍 Récupération des zones externes pour la catégorie : {}", category);
        return externalApi.listRemoteZones(category)
                .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Flux<Map<String, Object>> getAllExternalZones(String category) {
        log.info("🔍 Récupération des zones externes pour la catégorie : {}", category);
        return externalApi.listRemoteZones(category)
                .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Mono<Map<String, Object>> getExternalZoneDetails(String type, UUID id) {
        log.info("ℹ️ Récupération des détails de la zone : {} (Type: {})", id, type);
        return externalApi.getRemoteZoneDetails(type, id)
                .switchIfEmpty(Mono.error(new RuntimeException("Zone introuvable sur le moteur externe")));
    };

    private GeofenceZone mapRemoteToDomain(UUID id, UUID fleetId, UUID managerId, Map<String, Object> remote) {
        // On extrait les coordonnées si c'est un cercle
        Double lat = null;
        Double lng = null;
        if (remote.get("center") instanceof Map<?, ?> center) {
            List<?> coords = (List<?>) center.get("coordinates");
            if (coords != null && coords.size() >= 2) {
                lng = Double.valueOf(coords.get(0).toString());
                lat = Double.valueOf(coords.get(1).toString());
            }
        }

        return new GeofenceZone(
                id,
                fleetId,
                managerId,
                (String) remote.getOrDefault("title", "Zone"),
                (String) remote.get("description"),
                (String) remote.get("type"),
                lat, lng,
                remote.get("radius") != null ? Double.valueOf(remote.get("radius").toString()) : null,
                (Boolean) remote.getOrDefault("isTemporalEnabled", false),
                null, null, null, // temporal data à parser si besoin
                (Boolean) remote.getOrDefault("isConditionalEnabled", false),
                null, null, null,
                (Boolean) remote.getOrDefault("isActive", true),
                null, null,
                null // Vertices à parser depuis le champ 'polygon' si besoin
        );
    }

    @Override
    public Flux<GeofenceZone> getMyZones(UUID managerId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMyZones'");
    }

    private Flux<Map<String, Object>> getFilteredRemoteZones(UUID managerId, String category, UUID optionalFleetId) {
        // 1. Récupérer les IDs autorisés en local
        Flux<GeofenceZoneEntity> localLinks = (optionalFleetId == null)
                ? zoneRepo.findByManagerId(managerId)
                : zoneRepo.findAllByManagerIdAndFleetId(managerId, optionalFleetId);

        return localLinks.map(GeofenceZoneEntity::getId)
                .collect(Collectors.toSet())
                .flatMapMany(myIds -> {
                    if (myIds.isEmpty())
                        return Flux.empty();
                    // 2. Appel distant et intersection
                    return externalApi.listRemoteZones(category)
                            .flatMapMany(Flux::fromIterable)
                            .filter(remoteMap -> {
                                String rId = (String) remoteMap.get("id");
                                return rId != null && myIds.contains(UUID.fromString(rId));
                            });
                });
    }

    @Override // GET ALL par Flotte pour un manager
    public Flux<Map<String, Object>> getZonesByFleet(UUID managerId, UUID fleetId) {
        return getFilteredRemoteZones(managerId, "all", fleetId);
    }

    @Override
    public Flux<GeofenceZone> getZonesByFleet1(UUID fleetId) {
        return localPersistence.findByFleetId(fleetId)
                .flatMap(localLink -> externalApi.getRemoteZoneDetails("all", localLink.id())
                        .map(details -> mapRemoteToDomain(localLink.id(), fleetId, localLink.managerId(), details))
                        .onErrorResume(e -> Mono.empty()));
    }

    @Override // GET by ID (Sécurisé par managerId)
    public Mono<Map<String, Object>> getZoneDetails(UUID zoneId, UUID managerId) {
        return zoneRepo.findById(zoneId)
                .filter(link -> link.getManagerId().equals(managerId))
                .switchIfEmpty(Mono.error(new RuntimeException("Zone introuvable ou accès refusé.")))
                .flatMap(link -> externalApi.listRemoteZones("all")
                        .flatMapMany(Flux::fromIterable)
                        .filter(remoteMap -> zoneId.toString().equals(remoteMap.get("id")))
                        .next());
    }

    @Override
    public Mono<Void> deleteZone(UUID zoneId, String type, UUID managerId) {
        return zoneRepo.findById(zoneId)
                .filter(z -> z.getManagerId().equals(managerId))
                .flatMap(link -> externalApi.deleteRemoteZone(type, zoneId)
                        .then(zoneRepo.deleteById(zoneId)));
    }

    @Override
    public Flux<Map<String, Object>> getZonesByManager(UUID managerId, String category) {
        // On délègue à l'adaptateur qui utilisera le System Token pour récupérer les
        // zones de ce manager
        return externalApi.getZonesByOwner(managerId, category)
                .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Flux<Map<String, Object>> getZonesByFleetManager(UUID fleetManagerId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getZonesByFleetManager'");
    }

    @Override
    public Mono<String> checkPointInZone(UUID zoneId, Double lat, Double lng) {
        return externalApi.checkPointInZone(zoneId, lat, lng);
    }

    @Override
    public Mono<Map<String, Object>> getExternalAlerts(int p, int s) {
        return externalApi.fetchRemoteAlerts(p, s);
    }

    @Override
    public Mono<Void> updateRemoteZone(String t, UUID id, Map<String, Object> u) {
        return externalApi.updateRemoteZone(t, id, u);
    }

    @Override
    public Flux<GeofenceEventEntity> getEvents(UUID v, UUID z, String t, LocalDate d) {
        return localPersistence.findEventsWithFilters(v, z, t, d);
    }

  @Override
  @Transactional
    public Mono<Void> assignZoneToFleet(UUID zoneId, UUID fleetId, UUID managerId) {
        return zoneRepo.findById(zoneId)
                .filter(z -> z.getManagerId().equals(managerId))
                .switchIfEmpty(Mono.error(new RuntimeException("Zone introuvable ou non autorisée")))
                .flatMap(z -> {
                    z.setFleetId(fleetId);
                    z.setNew(false);
                    return zoneRepo.save(z)
                        .then(syncFleetVehiclesToZone(fleetId, zoneId, z.getZoneType()));
                }).then();
    }

    private Mono<Void> syncFleetVehiclesToZone(UUID fleetId, UUID zoneId, String zoneType) {
        log.info("🔄 Début de l'assignation des véhicules de la flotte {} à la zone {}", fleetId, zoneId);

        // On récupère tous les véhicules de la flotte en base locale
        return vehiclePersistencePort.getAllVehicles() // Utilisation du port, pas repository direct
                .filter(v -> fleetId.equals(v.fleetId())) // Filtre en mémoire ou optimiser repository
                .filter(v -> v.geofenceRemoteId() != null) // On ne traite que ceux qui ont un ID Geofence
                .flatMap(vehicle -> {
                    log.debug("👉 Ajout du véhicule {} ({}) à la zone", vehicle.licensePlate(),
                            vehicle.geofenceRemoteId());
                    return externalApi.addVehicleToZone(vehicle.geofenceRemoteId(), zoneId, zoneType)
                            .onErrorResume(e -> {
                                log.warn("⚠️ Échec pour le véhicule {}: {}", vehicle.licensePlate(), e.getMessage());
                                return Mono.empty(); // On continue malgré l'erreur
                            });
                })
                .then();
    }

}