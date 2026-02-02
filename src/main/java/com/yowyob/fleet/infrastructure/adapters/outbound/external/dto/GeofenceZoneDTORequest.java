package com.yowyob.fleet.infrastructure.adapters.outbound.external.dto;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record GeofenceZoneDTORequest (
   String type,
    String title,
    String description,
    Boolean isTemporalEnabled,
    Boolean isConditionalEnabled,
    LocalTime startTime,
    LocalTime endTime,
    PolygonData polygon // Pour capturer l'objet imbriqué "polygon"
) {
    public record PolygonData(
        String type,
        List<List<List<Double>>> coordinates // Format [[[lng, lat], ...]]
    ) {}
}