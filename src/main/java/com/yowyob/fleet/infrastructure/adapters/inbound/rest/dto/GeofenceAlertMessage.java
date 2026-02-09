package com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;

/**
 * ReprÃ©sentation du message JSON envoyÃ© par le moteur Geofence sur le topic Kafka.
 */
public record GeofenceAlertMessage(
    @JsonProperty("vehicleId") UUID vehicleId, // ID distant (ex: geofenceRemoteId)
    @JsonProperty("zoneId") UUID zoneId,
    @JsonProperty("type") String type, // ENTRY ou EXIT
    @JsonProperty("timestamp") Instant timestamp,
    @JsonProperty("speed") Double speed,
    @JsonProperty("coordinates") Coordinates coordinates
) {
    public record Coordinates(Double lat, Double lng) {}
}