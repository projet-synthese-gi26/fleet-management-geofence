package com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto;

import java.time.LocalDate;

public record MaintenanceUpdateRequest(
    LocalDate lastMaintenanceDate,
    LocalDate nextMaintenanceDue,
    String engineStatus,
    Integer batteryHealth,
    String maintenanceStatus
) {}