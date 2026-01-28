package com.yowyob.fleet.application.service;

import com.yowyob.fleet.domain.model.Trip;
import com.yowyob.fleet.domain.ports.in.ManageTripUseCase;
import com.yowyob.fleet.domain.ports.out.DistanceCalculatorPort;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.RedisTelemetryAdapter;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.TripEntity;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.TripR2dbcRepository;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.VehicleLocalR2dbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripService implements ManageTripUseCase {

    private final TripR2dbcRepository tripRepository;
    private final VehicleLocalR2dbcRepository vehicleRepository;
    private final RedisTelemetryAdapter redisTelemetry;
    private final DistanceCalculatorPort distanceCalculator;

    @Override
    @Transactional
    public Mono<Trip> startTrip(UUID driverId, UUID vehicleId) {
        // 1. Vérifications : Chauffeur déjà en course ? Véhicule déjà pris ?
        return tripRepository.findByDriverIdAndStatus(driverId, "ONGOING")
            .flatMap(t -> Mono.error(new IllegalStateException("Vous avez déjà une course en cours !")))
            .switchIfEmpty(
                vehicleRepository.findById(vehicleId)
                    .filter(v -> "AVAILABLE".equals(v.getStatus()))
                    .switchIfEmpty(Mono.error(new IllegalStateException("Véhicule non disponible ou inexistant")))
            )
            .then(Mono.defer(() -> {
                // Capture du temps UTC
                Instant now = Instant.now();
                LocalDate today = LocalDate.ofInstant(now, ZoneOffset.UTC);
                LocalTime currentTime = LocalTime.ofInstant(now, ZoneOffset.UTC);

                // 2. Création du Trip
                TripEntity trip = new TripEntity();
                trip.setId(UUID.randomUUID());
                trip.setDriverId(driverId);
                trip.setVehicleId(vehicleId);
                
                // Correction du typage ici : Instant -> LocalDate / LocalTime
                trip.setStartDate(today);
                trip.setStartTime(currentTime);
                
                trip.setStatus("ONGOING");
                trip.setNew(true); // Force INSERT pour R2DBC

                // 3. Mise à jour Véhicule -> ON_TRIP
                return vehicleRepository.findById(vehicleId)
                        .flatMap(v -> {
                            v.setStatus("ON_TRIP");
                            // IMPORTANT : isNew false car c'est un update
                            v.setNew(false); 
                            return vehicleRepository.save(v);
                        })
                        .then(tripRepository.save(trip));
            }))
            .map(this::mapToDomain);
    }

    @Override
    public Mono<Void> sendTelemetry(UUID tripId, Double lat, Double lng, Double speed) {
        // 1. Mise à jour Redis (Historique pour distance)
        return redisTelemetry.addPoint(tripId, lat, lng)
            // 2. TODO: Mise à jour OperationalParams dans Postgres (Dernière position) au prochain Jalon
            .then();
    }

    @Override
    @Transactional
    public Mono<Trip> endTrip(UUID tripId) {
        return tripRepository.findById(tripId)
            .filter(t -> "ONGOING".equals(t.getStatus()))
            .switchIfEmpty(Mono.error(new IllegalStateException("Trajet introuvable ou déjà terminé")))
            .flatMap(trip -> {
                // 1. Calcul Distance via Redis
                return redisTelemetry.getTripPath(tripId)
                    .collectList()
                    .map(points -> distanceCalculator.calculateTotalDistanceKm(points))
                    .flatMap(distance -> {
                        // Capture du temps de fin UTC
                        Instant now = Instant.now();
                        
                        // 2. Mise à jour Trip
                        trip.setEndDate(LocalDate.ofInstant(now, ZoneOffset.UTC));
                        trip.setEndTime(LocalTime.ofInstant(now, ZoneOffset.UTC));
                        trip.setStatus("COMPLETED");
                        trip.setDistanceKm(distance); 
                        trip.setNew(false); // Update

                        // 3. Libération Véhicule
                        return vehicleRepository.findById(trip.getVehicleId())
                                .flatMap(v -> {
                                    v.setStatus("AVAILABLE");
                                    v.setNew(false);
                                    return vehicleRepository.save(v);
                                })
                                .then(tripRepository.save(trip))
                                // 4. Nettoyage Redis (Fire & Forget)
                                .doOnSuccess(t -> redisTelemetry.clearTripPath(tripId).subscribe());
                    });
            })
            .map(this::mapToDomain);
    }

    @Override
    public Mono<Trip> getCurrentTrip(UUID driverId) {
        return tripRepository.findByDriverIdAndStatus(driverId, "ONGOING")
                .map(this::mapToDomain);
    }

    @Override
    public Mono<Trip> getTripById(UUID tripId) {
        return tripRepository.findById(tripId)
                .map(this::mapToDomain);
    }

    // Helper Mapper
    private Trip mapToDomain(TripEntity e) {
        return new Trip(
            e.getId(), 
            e.getVehicleId(),
            e.getDriverId(),
            e.getStatus(),
            e.getStartDate(),
            e.getStartTime(),
            e.getEndDate(),
            e.getEndTime(),
            e.getDistanceKm(),
            e.getDurationMinutes()
        );
    }
}