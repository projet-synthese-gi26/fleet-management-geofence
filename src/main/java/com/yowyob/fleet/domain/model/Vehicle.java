package com.yowyob.fleet.domain.model;

import java.util.UUID;

public record Vehicle(
    UUID id,
    UUID fleetId,
    UUID currentDriverId,
    UUID vehicleTypeId,
    String licensePlate,
    String brand,
    String model,
    Integer manufacturingYear,
    String type, // Libellé technique (ex: CAR)
    String color,
    String status, // AVAILABLE, ON_TRIP, MAINTENANCE
    String photoUrl,
    VehicleParameters.Financial financialParameters,
    VehicleParameters.Maintenance maintenanceParameters,
    VehicleParameters.Operational operationalParameters
) {}