package com.yowyob.fleet.application.service;

import com.yowyob.fleet.domain.model.GeofenceZone;
import com.yowyob.fleet.domain.ports.in.ManageGeofenceUseCase;
import com.yowyob.fleet.domain.ports.out.ExternalGeofencePort;
import com.yowyob.fleet.domain.ports.out.GeofencePersistencePort;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.GeofenceEventEntity;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.GeofenceZoneEntity;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.FleetEntity;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.FleetR2dbcRepository;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.GeofenceR2dbcRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
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
    private final FleetR2dbcRepository fleetRepo;
    private final GeofenceR2dbcRepository zoneRepo;

    @Override
    @Transactional
    public Mono<GeofenceZone> createZone(GeofenceZone zoneInput, UUID managerId) {
        log.info("🚀 Création d'une zone Geofence pour le manager: {}", managerId);

        // 1. On cherche la flotte du manager (Source de parenté locale)
        return externalApi.synchronizeZone(zoneInput)
                .then(Mono.defer(() -> {
                    // 2. Sauvegarde du lien (Manager + Flotte optionnelle)
                    GeofenceZoneEntity link = GeofenceZoneEntity.builder()
                            .id(zoneInput.id())
                            .managerId(managerId)
                            .fleetId(zoneInput.fleetId()) // Peut être null
                            .isNew(true)
                            .build();
                    return zoneRepo.save(link);
                }))
                .thenReturn(zoneInput);
    }

    public Mono<Void> assignZoneToFleet(UUID zoneId, UUID fleetId, UUID managerId) {
        return zoneRepo.findById(zoneId)
                .filter(z -> z.getManagerId().equals(managerId)) // Sécurité
                .switchIfEmpty(Mono.error(new RuntimeException("Zone introuvable ou non autorisée")))
                .flatMap(z -> {
                    z.setFleetId(fleetId);
                    z.setNew(false);
                    return zoneRepo.save(z);
                }).then();
    }

    @Override
    public Flux<GeofenceZone> getZonesByFleet(UUID fleetId) {
        // 1. Récupérer les IDs locaux liés à la flotte
        return localPersistence.findByFleetId(fleetId)
                // 2. Pour chaque ID, aller chercher les détails distants
                .flatMap(localLink -> externalApi.getRemoteZoneDetails("all", localLink.id()) // "all" ou type
                                                                                              // spécifique si stocké
                        .map(details -> mapRemoteToDomain(localLink.id(), localLink.fleetId(), details))
                        // Si une zone n'existe plus en distant, on l'ignore ou on nettoie
                        .onErrorResume(e -> Mono.empty()));
    }

    @Override
    public Flux<GeofenceZone> getZonesByFleetManager(UUID fleetManagerId) {
        log.info("Récupération des zones pour le FleetManager: {}", fleetManagerId);
        return localPersistence.findZonesByFleetManagerId(fleetManagerId);
    }

    @Override
    public Mono<GeofenceZone> getZoneDetails(UUID zoneId) {
        return Mono.empty();
    }

    @Override
    public Mono<GeofenceZone> updateZone(UUID zoneId, GeofenceZone zone) {
        // On force l'ID pour être sûr de mettre à jour la même zone
        GeofenceZone updatedZone = new GeofenceZone(
                zoneId, zone.fleetId(), zone.name(), zone.description(),
                zone.zoneType(), zone.centerLatitude(), zone.centerLongitude(),
                zone.radius(), zone.isTemporalEnabled(), zone.startTime(),
                zone.endTime(), zone.activeDays(), zone.isConditionalEnabled(),
                zone.maxSpeed(), zone.maxDwellTime(), zone.minDwellTime(),
                zone.isActive(), zone.surfaceArea(), zone.perimeter(), zone.vertices());

        return externalApi.synchronizeZone(updatedZone)
                .thenReturn(updatedZone);
    }


    @Override
    public Flux<Map<String, Object>> getMyZones(UUID managerId, String category) {
        // 1. On récupère les IDs des zones de ce manager uniquement
        return zoneRepo.findAllByManagerId(managerId)
                .map(GeofenceZoneEntity::getId)
                .collectList()
                .flatMapMany(myIds -> {
                    if (myIds.isEmpty())
                        return Flux.empty();
                    // 2. Bulk fetch distant et filtre
                    return externalApi.listRemoteZones(category)
                            .flatMapMany(Flux::fromIterable)
                            .filter(remoteMap -> myIds.contains(UUID.fromString(remoteMap.get("id").toString())));
                });
    }

    // --- DÉTAILS (FIX RÉPONSE VIDE) ---
@Override
public Mono<Map<String, Object>> getExternalZoneDetails(String type, UUID id) {
    log.info("🔍 Récupération sécurisée des détails de la zone: {} (Type: {})", id, type);
    
    // APPEL DIRECT : On utilise l'adapter pour interroger le moteur sur cet ID précis
    return externalApi.getRemoteZoneDetails(type, id)
            .switchIfEmpty(Mono.error(new RuntimeException("Zone introuvable sur le moteur externe pour l'ID: " + id)))
            .onErrorResume(e -> {
                log.error("❌ Erreur lors de la récupération de la zone {}: {}", id, e.getMessage());
                return Mono.error(new RuntimeException("Zone introuvable ou vous n'avez pas les droits. Details: " + e.getMessage()));
            });
}

    private GeofenceZone mapRemoteToDomain(UUID id, UUID fleetId, Map<String, Object> remote) {
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
                fleetId, // On passe le fleetId récupéré de NOTRE base de données
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
            ? zoneRepo.findAllByManagerId(managerId)
            : zoneRepo.findAllByManagerIdAndFleetId(managerId, optionalFleetId);

        return localLinks.map(GeofenceZoneEntity::getId)
                .collect(Collectors.toSet())
                .flatMapMany(myIds -> {
                    if (myIds.isEmpty()) return Flux.empty();
                    // 2. Appel distant et intersection
                    return externalApi.listRemoteZones(category)
                            .flatMapMany(Flux::fromIterable)
                            .filter(remoteMap -> {
                                String rId = (String) remoteMap.get("id");
                                return rId != null && myIds.contains(UUID.fromString(rId));
                            });
                });
    }
    @Override // GET ALL ou par TYPE (circles/polygons) pour un manager
    public Flux<Map<String, Object>> getZonesByManager(UUID managerId, String category) {
        return getFilteredRemoteZones(managerId, category, null);
    }

    @Override // GET ALL par Flotte pour un manager
    public Flux<Map<String, Object>> getZonesByFleet(UUID managerId, UUID fleetId) {
        return getFilteredRemoteZones(managerId, "all", fleetId);
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
    @Override public Mono<String> checkPointInZone(UUID zoneId, Double lat, Double lng) { return externalApi.checkPointInZone(zoneId, lat, lng); }
    @Override public Mono<Map<String, Object>> getExternalAlerts(int p, int s) { return externalApi.fetchRemoteAlerts(p, s); }
    @Override public Mono<Void> updateRemoteZone(String t, UUID id, Map<String, Object> u) { return externalApi.updateRemoteZone(t, id, u); }
    @Override public Flux<GeofenceEventEntity> getEvents(UUID v, UUID z, String t, LocalDate d) { return localPersistence.findEventsWithFilters(v, z, t, d); }

 @Override
public Flux<Map<String, Object>> getAllExternalZones(UUID managerId, String category) {
    log.info("🛡️ Filtrage direct par Manager : {}", managerId);

    // 1. Récupérer les IDs des zones appartenant à ce manager en local
    return zoneRepo.findAllByManagerId(managerId)
        .map(GeofenceZoneEntity::getId)
        .collect(Collectors.toSet())
        .flatMapMany(localIds -> {
            if (localIds.isEmpty()) {
                log.info("ℹ️ Aucune zone locale trouvée pour le manager {}", managerId);
                return Flux.empty();
            }
            
            // 2. Récupérer tout le contenu externe et filtrer par les IDs trouvés
            return externalApi.listRemoteZones("all")
                .flatMapMany(Flux::fromIterable)
                .filter(extZone -> {
                    Object id = extZone.get("id");
                    return id != null && localIds.contains(UUID.fromString(id.toString()));
                });
        })
        .doOnNext(zone -> log.info("✅ Zone autorisée trouvée : {}", zone.get("id")));
}
@Override
public Flux<Map<String, Object>> getAllExternalZones(String category) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getAllExternalZones'");
}

}