package com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;
import java.time.LocalDate;
import java.util.UUID;

@Table("maintenance_parameters")
@Data @NoArgsConstructor @AllArgsConstructor
public class MaintenanceParameterEntity {
    @Id
    private UUID id;
    @Column("vehicle_id")
    private UUID vehicleId;
    @Column("last_maintenance_at")
    private LocalDate lastMaintenanceAt;
    @Column("next_maintenance_at")
    private LocalDate nextMaintenanceAt;
    @Column("engine_status")
    private String engineStatus; 
    @Column("battery_health")
    private String batteryHealth;
    @Column("maintenance_status")
    private String maintenanceStatus;
}