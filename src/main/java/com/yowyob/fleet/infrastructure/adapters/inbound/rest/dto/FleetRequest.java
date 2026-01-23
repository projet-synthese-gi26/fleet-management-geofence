package com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record FleetRequest(
    @NotBlank(message = "Le nom de la flotte est obligatoire") 
    String name,
    
    @NotNull(message = "L'ID du Manager est obligatoire") 
    UUID managerId,
    
    String phoneNumber
) {}