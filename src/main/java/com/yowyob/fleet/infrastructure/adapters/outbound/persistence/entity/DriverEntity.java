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

    @Override
    public UUID getId() { return userId; }

    @Override
    @Transient
    public boolean isNew() { return isNewRecord; } // Utilise le nouveau nom

    public void setNewRecord(boolean isNew) { this.isNewRecord = isNew; }
}