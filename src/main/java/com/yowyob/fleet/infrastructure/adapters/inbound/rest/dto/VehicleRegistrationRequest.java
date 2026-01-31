package com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record VehicleRegistrationRequest(
    // --- CHAMPS TECHNIQUES (Pour le service distant) ---

    @NotBlank(message = "La marque est obligatoire (ex: Toyota)")
    @Schema(example = "Toyota", description = "Marque du constructeur")
    String brand,

    @NotBlank(message = "Le modèle est obligatoire (ex: Yaris)")
    @Schema(example = "Yaris", description = "Modèle du véhicule")
    String model,

    @NotBlank(message = "L'immatriculation est obligatoire")
    @Schema(example = "LT-123-AB", description = "Plaque d'immatriculation unique")
    String licensePlate,

    @NotBlank(message = "Le type de carburant est obligatoire")
    @Schema(example = "Essence", description = "Essence, Diesel, Hybride, Electrique")
    String fuelType,

    @Schema(example = "Manuelle", defaultValue = "Manuelle", description = "Manuelle ou Automatique")
    String transmissionType,

    // --- CHAMPS LOCAUX (Pour notre base Fleet) ---

    @NotNull(message = "Le type de véhicule (Catégorie) est obligatoire")
    @Schema(description = "ID de la catégorie locale (ex: ID pour 'Poids Lourd')", example = "11111111-1111-1111-1111-111111111111")
    UUID vehicleTypeId,

    @Schema(description = "URL de la photo (facultatif)", example = "https://images.com/mycar.jpg")
    String photoUrl
) {}