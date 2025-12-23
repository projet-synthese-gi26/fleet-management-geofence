package com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;
import java.util.UUID;

@Table("vehicles")
@Data @NoArgsConstructor @AllArgsConstructor
public class VehicleLocalEntity {
    @Id
    private UUID id; // This must match the External ID
    @Column("fleet_id")
    private UUID fleetId;
    @Column("driver_id")
    private UUID driverId;
    @Column("user_id")
    private UUID userId;
    // Note: brand, model, color are NOT here because they are remote
}