package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import com.yowyob.fleet.domain.model.Trip;
import com.yowyob.fleet.domain.ports.in.ManageTripUseCase;
import com.yowyob.fleet.domain.ports.out.AuthPort;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.StartTripRequest;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.TelemetryRequest;
import com.yowyob.fleet.infrastructure.config.OpenApiConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
@Tag(name = OpenApiConfig.TAG_TRIPS , description = "Gestion des courses et télémétrie")
@SecurityRequirement(name = "bearerAuth")
public class TripController {

    private final ManageTripUseCase tripUseCase;

    private UUID getUserId(Authentication auth) {
        return ((AuthPort.UserDetail) auth.getPrincipal()).id();
    }

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('FLEET_DRIVER')")
    @Operation(summary = "Démarrer une course (Driver)")
    public Mono<Trip> start(@RequestBody StartTripRequest request, Authentication auth) {
        return tripUseCase.startTrip(getUserId(auth), request.vehicleId());
    }

    @PostMapping("/{id}/telemetry")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasRole('FLEET_DRIVER')")
    @Operation(summary = "Envoyer un point GPS (Driver)")
    public Mono<Void> telemetry(@PathVariable UUID id, @RequestBody TelemetryRequest request) {
        return tripUseCase.sendTelemetry(id, request.lat(), request.lng(), request.speed());
    }

    @PostMapping("/{id}/end")
    @PreAuthorize("hasRole('FLEET_DRIVER')")
    @Operation(summary = "Terminer une course (Driver)")
    public Mono<Trip> end(@PathVariable UUID id) {
        return tripUseCase.endTrip(id);
    }

    @GetMapping("/current")
    @PreAuthorize("hasRole('FLEET_DRIVER')")
    @Operation(summary = "Récupérer ma course en cours (Driver)")
    public Mono<Trip> getCurrent(Authentication auth) {
        return tripUseCase.getCurrentTrip(getUserId(auth));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('FLEET_MANAGER', 'FLEET_ADMIN')")
    @Operation(summary = "Détail d'une course (Manager)")
    public Mono<Trip> getById(@PathVariable UUID id) {
        return tripUseCase.getTripById(id);
    }
}