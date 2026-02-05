package com.yowyob.fleet.infrastructure.adapters.outbound.external.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.yowyob.fleet.infrastructure.adapters.outbound.external.dto.GeofenceZoneDTORequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@HttpExchange("/api")
public interface GeofenceApiClient {


    @PostExchange("/geofence")
    Mono<java.util.Map<String,Object>> createZone(
        @RequestBody Object request,
        @RequestHeader("Authorization") String token
    );

    @GetExchange("/check")
    Mono<String> checkPoint(
        @RequestParam("zoneId") String zoneId, // Utilisation de String pour plus de souplesse
        @RequestParam("lat") Double lat,
        @RequestParam("lng") Double lng,
        @RequestHeader("Authorization") String token
    );

     @GetExchange("/geofence")
    Mono<JsonNode> getAllZones(@RequestHeader("Authorization") String token);

    @GetExchange("/geofence/circles")
    Mono<JsonNode> getCircles(@RequestHeader("Authorization") String token);

    @GetExchange("/geofence/polygons")
    Mono<JsonNode> getPolygons(@RequestHeader("Authorization") String token);

    // --- UNITAIRE ---
    @GetExchange("/geofence/{type}/{id}")
    Mono<JsonNode> getZoneById(
        @PathVariable("type") String type, 
        @PathVariable("id") UUID id, 
        @RequestHeader("Authorization") String token
    );

 
    @PutExchange("/geofence/{type}/{id}")
    Mono<Void> updateZone(
        @PathVariable("type") String type, 
        @PathVariable("id") UUID id, 
        @RequestBody Object request, 
        @RequestHeader("Authorization") String token
    );

    @DeleteExchange("/geofence/{type}/{id}")
    Mono<Void> deleteZone(
        @PathVariable("type") String type, 
        @PathVariable("id") UUID id, 
        @RequestHeader("Authorization") String token
    );

    // --- ALERTS ---
    @GetExchange("/alerts")
    Mono<Map<String, Object>> getAlerts(
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") int size,
        @RequestHeader("Authorization") String token
    );
    
}