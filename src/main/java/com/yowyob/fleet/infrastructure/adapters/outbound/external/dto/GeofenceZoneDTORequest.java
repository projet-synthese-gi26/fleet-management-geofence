package com.yowyob.fleet.infrastructure.adapters.outbound.external.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.List;

@Schema(description = "Requête de création d'une zone de Geofencing")
public record GeofenceZoneDTORequest(
    
    @Schema(example = "POLYGON", allowableValues = {"CIRCLE", "POLYGON"})
    @NotBlank String type,

    @Schema(example = "Zone Bastos Sécurité")
    @NotBlank String title,

    @Schema(example = "Surveillance du quartier résidentiel")
    String description,

    @Schema(description = "Active la restriction horaire", example = "true")
    Boolean isTemporalEnabled,

    @Schema(description = "Active les conditions de vitesse/temps", example = "false")
    Boolean isConditionalEnabled,

    @Schema(type = "string", pattern = "HH:mm:ss", example = "08:00:00")
    LocalTime startTime,

    @Schema(type = "string", pattern = "HH:mm:ss", example = "18:00:00")
    LocalTime endTime,

    @Schema(description = "Données pour un cercle (obligatoire si type=CIRCLE)")
    CircleData center,

    @Schema(description = "Rayon en mètres (si type=CIRCLE)", example = "500")
    Double radius,

    @Schema(description = "Données pour un polygone (obligatoire si type=POLYGON)")
    PolygonData polygon
) {
    public record CircleData(
        @Schema(description = "Coordonnées [longitude, latitude]", example = "[11.5021, 3.8480]")
        List<Double> coordinates
    ) {}

    public record PolygonData(
        @Schema(example = "Polygon")
        String type,
        
        @Schema(description = "Liste de anneaux de coordonnées. Le premier est le contour extérieur.", 
                example = "[[[11.50, 3.84], [11.51, 3.84], [11.51, 3.85], [11.50, 3.85], [11.50, 3.84]]]")
        List<List<List<Double>>> coordinates
    ) {}
}