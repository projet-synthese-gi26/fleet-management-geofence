package com.yowyob.fleet.infrastructure.adapters.outbound.external.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Mappe la réponse du Service Véhicule Distant.
 * Basé sur le Swagger fourni (VehicleResponse).
 */
public record VehicleExternalResponse(
    UUID vehicleId,
    UUID vehicleMakeId,
    UUID vehicleModelId,
    String registrationNumber, // registrationNumber dans le swagger
    String vehicleSerialNumber,
    String brand, // brand dans le swagger
    // Le swagger ne renvoie pas explicitement "model" en string dans la réponse standard, 
    // mais "brand" semble être là. On fera avec ce qu'on a.
    String vehicleSerialPhoto,
    String registrationPhoto,
    Instant createdAt,
    Instant updatedAt
) {
    // Helper pour récupérer un affichage lisible si le modèle n'est pas renvoyé en clair
    public String getDisplayName() {
        return (brand != null ? brand : "Unknown") + " (" + registrationNumber + ")";
    }
}