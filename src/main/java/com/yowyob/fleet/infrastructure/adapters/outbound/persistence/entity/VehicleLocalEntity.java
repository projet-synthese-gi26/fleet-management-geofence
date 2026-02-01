package com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;
import java.util.UUID;

@Table(name = "vehicles", schema = "fleet")
@Data @NoArgsConstructor @AllArgsConstructor
public class VehicleLocalEntity implements Persistable<UUID> {
    @Id
    private UUID id;

    @Column("fleet_id")
    private UUID fleetId;

    @Column("manager_id")
    private UUID managerId;

    @Column("current_driver_id")
    private UUID currentDriverId;

    @Column("vehicle_type_id")
    private UUID vehicleTypeId;

    // --- CHAMPS AJOUTÉS (Manquaient pour le mapping) ---
    
    @Column("license_plate")
    private String licensePlate;

    private String brand;

    private String model;

    @Column("manufacturing_year")
    private Integer manufacturingYear;

    private String color;

    // ---------------------------------------------------

    private String status; // AVAILABLE, ON_TRIP, MAINTENANCE
    
    @Column("photo_url")
    private String photoUrl;

    @Transient
    private boolean isNew = false;

    @Override
    public boolean isNew() {
        return isNew || id == null;
    }
}