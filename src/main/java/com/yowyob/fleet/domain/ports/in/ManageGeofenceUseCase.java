package com.yowyob.fleet.domain.ports.in;

import com.yowyob.fleet.domain.model.GeofenceZone;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.GeofenceEventEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ManageGeofenceUseCase {

    // --- CRÉATION ---
    Mono<GeofenceZone> createZone(GeofenceZone zone);
    
    /**
     * Crée une zone et la lie à un FleetManager
     */
    Mono<GeofenceZone> createZoneWithFleetManager(GeofenceZone zone, UUID fleetManagerId);

    // --- LECTURE (Moteur Externe) ---
    /**
     * Récupère les zones depuis le moteur externe.
     * @param category : "all", "circles" ou "polygons"
     */
    Flux<Map<String, Object>> getAllExternalZones(String category);

    /**
     * Récupère le détail d'une zone spécifique par son type et son ID.
     */
    Mono<Map<String, Object>> getExternalZoneDetails(String type, UUID id);

    // --- MODIFICATION ---
    /**
     * Mise à jour partielle d'une zone sur le moteur externe.
     */
    Mono<Void> updateRemoteZone(String type, UUID id, Map<String, Object> updates);

    // --- SUPPRESSION ---
    /**
     * Supprime une zone sur le moteur externe.
     */
    Mono<Void> deleteZone(UUID zoneId, String type);

    // --- OPÉRATIONS SPATIALES & ALERTES ---
    /**
     * Vérifie en temps réel si un point est dans une zone.
     */
    Mono<String> checkPointInZone(UUID zoneId, Double lat, Double lng);

    /**
     * Récupère l'historique des alertes (violations de zone) paginé.
     */
    Mono<Map<String, Object>> getExternalAlerts(int page, int size);

    // --- PERSISTANCE LOCALE (Optionnel selon ton besoin) ---
    Flux<GeofenceEventEntity> getEvents(UUID vehicleId, UUID zoneId, String type, LocalDate date);
    
    // Anciennes méthodes à garder si tu les utilises encore pour la DB locale :
    Flux<GeofenceZone> getZonesByFleet(UUID fleetId);
    
    /**
     * Récupère les zones associées à un FleetManager via la table de liaison
     */
    Flux<GeofenceZone> getZonesByFleetManager(UUID fleetManagerId);
    
    Mono<GeofenceZone> getZoneDetails(UUID zoneId);
    Mono<GeofenceZone> updateZone(UUID zoneId, GeofenceZone zone);
}