package com.yowyob.fleet.infrastructure.adapters.outbound.external.client;

import com.yowyob.fleet.infrastructure.adapters.outbound.external.dto.VehicleExternalResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;
import java.util.UUID;

@HttpExchange("/vehicles")
public interface VehicleApiClient {

    @GetExchange("/{id}")
    Mono<VehicleExternalResponse> getVehicleById(@PathVariable UUID id);

    /**
     * Utilisation de l'endpoint "Simplified" du service distant.
     * Les paramètres sont passés en Query Params (?makeName=...&modelName=...)
     */
    @PostExchange("/simplified")
    Mono<VehicleExternalResponse> createVehicleSimplified(
        @RequestParam("makeName") String makeName,
        @RequestParam("modelName") String modelName,
        @RequestParam("transmissionType") String transmissionType,
        @RequestParam("fuelTypeName") String fuelTypeName,
        @RequestParam("registrationNumber") String registrationNumber,
        @RequestParam(value = "vehicleSerialNumber", required = false) String vehicleSerialNumber,
        @RequestParam(value = "registrationPhoto", required = false) String registrationPhoto,
        @RequestParam(value = "color", required = false) String color, // Si supporté, sinon ignoré
        @RequestParam(value = "brand", required = false) String brand // Redondant avec makeName souvent
    );

    @DeleteExchange("/{id}")
    Mono<Void> deleteVehicle(@PathVariable UUID id);
}