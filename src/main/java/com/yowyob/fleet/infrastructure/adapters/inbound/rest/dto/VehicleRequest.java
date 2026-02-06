package com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record VehicleRequest(
    @NotBlank(message = "La marque est obligatoire")
    @Schema(example = "Toyota")
    String brand,

    @NotBlank(message = "Le modèle est obligatoire")
    @Schema(example = "Yaris")
    String model,

    @NotBlank(message = "La plaque d'immatriculation est obligatoire")
    @Schema(example = "LT-123-AA")
    String licensePlate,

     @Schema(description = "ID de la zone Geofence à assigner lors de la création", example = "a1b2c3d4-...")
    UUID geofenceZoneId, 

    @Schema(example = "VIN-987654321")
    String vehicleSerialNumber,

    @NotNull(message = "Le type de véhicule local est obligatoire")
    UUID vehicleTypeId,

    @NotBlank(message = "Le fabricant est obligatoire")
    @Schema(example = "Toyota Motor Group")
    String manufacturerName,

    @NotBlank(message = "La taille/gabarit est obligatoire")
    @Schema(example = "Citadine Compacte")
    String sizeName,

    @NotBlank(message = "Le type d'usage est obligatoire")
    @Schema(example = "Personnel")
    String typeName,

    @NotBlank(message = "Le type de carburant est obligatoire")
    @Schema(example = "Essence")
    String fuelType,

    @Schema(example = "Manuelle")
    String transmissionType,

    @Schema(example = "Rouge")
    String color,

    @Schema(example = "2022")
    Integer manufacturingYear,

    @Schema(example = "AVAILABLE")
    String status,

    Double tankCapacity,
    Integer totalSeatNumber,
    Double averageFuelConsumption,
    
    String photoUrl,
    String serialNumberPhotoUrl,
    String registrationPhotoUrl
) {


  
}