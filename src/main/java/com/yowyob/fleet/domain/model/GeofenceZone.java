package com.yowyob.fleet.domain.model;

import java.util.List;
import java.util.UUID;

public record GeofenceZone(
    UUID id,
    UUID fleetId,
    String name,
    String description,
    String type, // POLYGON ou CIRCLE
    Double radius, // Uniquement pour CIRCLE
    Double surfaceArea,
    Double perimeter,
    List<GeofencePoint> vertices
) {}