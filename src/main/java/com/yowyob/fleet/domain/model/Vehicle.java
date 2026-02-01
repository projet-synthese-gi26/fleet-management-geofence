package com.yowyob.fleet.domain.model;

import java.util.UUID;

public record Vehicle(
    UUID id,
    UUID fleetId,
    UUID managerId, // Nouveau champ
    UUID currentDriverId,
    UUID vehicleTypeId,
    
    // Identification
    String licensePlate,
    String vehicleSerialNumber, // VIN (Nouveau)
    
    // Caractéristiques Techniques (Nouveaux champs)
    String brand,
    String model,
    Integer manufacturingYear,
    String transmissionType, // ex: Manuelle, Auto
    String fuelType,         // ex: Diesel
    Double tankCapacity,
    Integer totalSeatNumber,
    Double averageFuelConsumption,
    
    // Visuel & Docs
    String color,
    String status, // AVAILABLE, ON_TRIP, MAINTENANCE
    String photoUrl, // Photo principale (avant)
    String serialNumberPhotoUrl, // Photo VIN
    String registrationPhotoUrl, // Photo Carte Grise
    
    // Sous-objets
    VehicleParameters.Financial financialParameters,
    VehicleParameters.Maintenance maintenanceParameters,
    VehicleParameters.Operational operationalParameters
) {}