package com.yowyob.fleet.domain.ports.in;

import com.yowyob.fleet.domain.model.Trip;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface ManageTripUseCase {
    // Commandes (Driver)
    Mono<Trip> startTrip(UUID driverId, UUID vehicleId); // vehicleId optionnel si déjà assigné
    Mono<Void> sendTelemetry(UUID tripId, Double lat, Double lng, Double speed);
    Mono<Trip> endTrip(UUID tripId);

    // Requêtes (Getters)
    Mono<Trip> getCurrentTrip(UUID driverId);
    Mono<Trip> getTripById(UUID tripId);
}