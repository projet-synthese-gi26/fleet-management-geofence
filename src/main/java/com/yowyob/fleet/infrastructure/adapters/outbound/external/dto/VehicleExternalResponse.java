package com.yowyob.fleet.infrastructure.adapters.outbound.external.dto;

import java.util.UUID;

public record VehicleExternalResponse(
    UUID id,
    String licensePlate,
    String brand,
    String model,
    Integer manufacturingYear,
    String type, // Ex: "CAR"
    String color
) {}