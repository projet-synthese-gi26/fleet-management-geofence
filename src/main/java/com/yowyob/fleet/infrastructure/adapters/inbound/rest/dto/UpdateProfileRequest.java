package com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto;

import jakarta.validation.constraints.Email;

public record UpdateProfileRequest(
    // Infos Identité (Propagées vers Auth Service)
    String firstName,
    String lastName,
    String phone,
    @Email String email,
    
    // Infos Métier (Stockées localement)
    String companyName,    // Uniquement pour FLEET_MANAGER
    String licenceNumber   // Uniquement pour FLEET_DRIVER
) {}