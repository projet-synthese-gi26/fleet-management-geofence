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
    
    @Column("centerLatitude")
    private Double centerLatitude;
    
    @Column("centerLongitude")
    private Double centerLongitude;
    
    private Double radius;
    
    @Column("isTemporalEnabled")
    private Boolean isTemporalEnabled;
    
    @Column("startTime")
    private LocalTime startTime;
    
    @Column("endTime")
    private LocalTime endTime;
    
    @Column("isConditionalEnabled")
    private Boolean isConditionalEnabled;
    
    @Column("maxSpeed")
    private Double maxSpeed;
    
    @Column("maxDwellTime")
    private Integer maxDwellTime;

    @Column("minDwellTime")
    private Integer minDwellTime;
    
    @Column("isActive")
    private Boolean isActive;

    // Métadonnées géométriques optionnelles
    @Column("surfaceArea")
    private Double surfaceArea;
    private Double perimeter;

    
@Transient
@Builder.Default
private boolean isNew = false;

    @Override
    public boolean isNew() {
        return isNew || id == null;
    }

    public void markNew() {
        this.isNew = true;
    }
}