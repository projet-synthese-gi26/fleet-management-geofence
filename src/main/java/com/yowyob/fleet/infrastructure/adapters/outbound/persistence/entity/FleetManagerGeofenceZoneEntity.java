package com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table(name = "fleetmanager_geofence_zones", schema = "fleet")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FleetManagerGeofenceZoneEntity {
    
    @Id
    @Column("fleet_manager_id")
    private UUID fleetManagerId;
    
    @Column("zone_id")
    private UUID zoneId;
}
