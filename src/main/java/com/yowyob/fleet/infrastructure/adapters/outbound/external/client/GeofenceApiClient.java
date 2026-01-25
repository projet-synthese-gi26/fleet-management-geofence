package com.yowyob.fleet.infrastructure.adapters.outbound.external.client;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import java.util.UUID;

@HttpExchange("/api/v1")
public interface GeofenceApiClient {

    /**
     * Vérifie si un point est à l'intérieur d'une zone.
     * Basé sur les query params vus dans la doc.
     */
    @GetExchange("/check")
    Mono<String> isPointInZone(
        @RequestParam("zoneId") UUID zoneId, 
        @RequestParam("lat") Double lat, 
        @RequestParam("lng") Double lng
    );

    /**
     * Exemple : Création d'une zone sur le service de calcul distant.
     */
    @PostExchange("/zones")
    Mono<Void> registerZone(@RequestBody Object zoneRequest);
}