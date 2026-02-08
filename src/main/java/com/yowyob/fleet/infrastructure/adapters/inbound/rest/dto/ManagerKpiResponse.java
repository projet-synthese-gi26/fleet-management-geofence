package com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ManagerKpiResponse(
    @Schema(description = "Nombre total de flottes enregistrées", example = "2")
    long totalFleets,

    @Schema(description = "Nombre total de véhicules dans toutes les flottes", example = "25")
    long totalVehicles,

    @Schema(description = "Nombre total de chauffeurs actifs/recrutés", example = "18")
    long totalDrivers,

    @Schema(description = "Nombre de trajets actuellement en cours", example = "5")
    long activeTrips
) {}