package com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalTime;
import java.util.UUID;

@Table(name = "geofence_zones", schema = "fleet")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class GeofenceZoneEntity implements Persistable<UUID> {
    @Id
    private UUID id;
    
    @Column("fleet_id")
    private UUID fleetId;
    private String name;
    private String description;
    
    @Column("zone_type")
    private String zoneType;
    
    @Column("center_latitude")
    private Double centerLatitude;
    
    @Column("center_longitude")
    private Double centerLongitude;
    
    private Double radius;
    
    @Column("is_temporal_enabled")
    private Boolean isTemporalEnabled;
    
    @Column("start_time")
    private LocalTime startTime;
    
    @Column("end_time")
    private LocalTime endTime;
    
    @Column("is_conditional_enabled")
    private Boolean isConditionalEnabled;
    
    @Column("max_speed")
    private Double maxSpeed;
    
    @Column("max_dwell_time")
    private Integer maxDwellTime;
    
    @Column("is_active")
    private Boolean isActive;

    // Métadonnées géométriques optionnelles
    @Column("surface_area")
    private Double surfaceArea;
    private Double perimeter;

    @Transient
    private boolean isNew = false;

    @Override
    public boolean isNew() {
        return isNew || id == null;
    }

    public void markNew() {
        this.isNew = true;
    }
}