package com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record VehicleRegistrationRequest(
    @NotNull UUID vehicleId
) {}