package com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.util.UUID;

@Table(name = "drivers", schema = "fleet")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverEntity implements Persistable<UUID> {
    @Id
    @Column("user_id")
    private UUID userId;
    
    @Column("fleet_id")
    private UUID fleetId;
    
    @Column("licence_number")
    private String licenceNumber;
    
    private String status;
    
    @Column("assigned_vehicle_id")
    private UUID assignedVehicleId;

    @Column("photo_url")
    private String photoUrl;

    @Transient
    private boolean isNewRecord = false; // Renommé ici

    public void setNewRecord(boolean isNew) { this.isNewRecord = isNew; }
    @Transient
    private boolean isNew = false;

    // Constructeur pour création
    public DriverEntity(UUID userId, String licenceNumber, String status, UUID assignedVehicleId) {
        this.userId = userId;
        this.licenceNumber = licenceNumber;
        this.status = status;
        this.assignedVehicleId = assignedVehicleId;
        this.isNew = true;
    }

    @Override
    public UUID getId() {
        return userId;
    }

    @Override
    public boolean isNew() {
        return isNew || userId == null;
    }
}