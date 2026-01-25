package com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.util.UUID;

@Table(name = "geofence_zones", schema = "fleet")
@Data @NoArgsConstructor @AllArgsConstructor
public class GeofenceZoneEntity {
    @Id
    private UUID id;
    @Column("fleet_id")
    private UUID fleetId;
    private String name;
    private String description;
    @Column("surface_area")
    private Double surfaceArea;
    private Double perimeter;
}