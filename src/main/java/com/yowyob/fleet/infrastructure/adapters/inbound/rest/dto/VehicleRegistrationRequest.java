package com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record VehicleRegistrationRequest(
    @NotNull(message = "L'ID technique du véhicule est obligatoire") 
    UUID vehicleId,
    
    @NotNull(message = "Le type de véhicule est obligatoire") 
    UUID vehicleTypeId,
    
    String photoUrl
) {}