package com.yowyob.fleet.domain.model;

import java.util.UUID;

public record Driver(
    UUID userId,
    UUID fleetId, // Ajouté
    String licenceNumber,
    Boolean status,
    UUID assignedVehicleId,
    String photoUrl // Ajouté
) {}