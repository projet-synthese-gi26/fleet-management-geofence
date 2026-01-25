package com.yowyob.fleet.infrastructure.mappers;

import com.yowyob.fleet.domain.model.Vehicle;
import com.yowyob.fleet.domain.model.VehicleParameters;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class VehicleLocalMapper {

    // --- Vers ENTITY (Pivot principal) ---

    @Mapping(target = "id", source = "id")
    @Mapping(target = "fleetId", source = "fleetId")
    @Mapping(target = "currentDriverId", source = "currentDriverId")
    @Mapping(target = "vehicleTypeId", source = "vehicleTypeId")
    // SUPPRESSION DES MAPPINGS INEXISTANTS (licensePlate, brand, model, color, year)
    // Ces données ne sont pas stockées dans VehicleLocalEntity
    @Mapping(target = "status", source = "status")
    @Mapping(target = "photoUrl", source = "photoUrl")
    @Mapping(target = "new", ignore = true) 
    public abstract VehicleLocalEntity toVehicleEntity(Vehicle domain);

    // --- Vers ENTITY (Paramètres Financiers) ---

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicleId", source = "id")
    @Mapping(target = "insuranceNumber", source = "financialParameters.insuranceNumber")
    @Mapping(target = "insuranceExpiredAt", source = "financialParameters.insuranceExpiryDate")
    @Mapping(target = "registeredAt", source = "financialParameters.registrationDate")
    @Mapping(target = "purchasedAt", source = "financialParameters.purchaseDate")
    @Mapping(target = "depreciationRate", source = "financialParameters.depreciationRate")
    @Mapping(target = "costPerKm", source = "financialParameters.costPerKm")
    public abstract FinancialParameterEntity toFinancialEntity(Vehicle domain);

    // --- Vers ENTITY (Paramètres Maintenance) ---

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicleId", source = "id")
    @Mapping(target = "lastMaintenanceAt", source = "maintenanceParameters.lastMaintenanceDate")
    @Mapping(target = "nextMaintenanceAt", source = "maintenanceParameters.nextMaintenanceDue")
    @Mapping(target = "engineStatus", source = "maintenanceParameters.engineStatus")
    @Mapping(target = "batteryHealth", source = "maintenanceParameters.batteryHealth")
    @Mapping(target = "maintenanceStatus", source = "maintenanceParameters.maintenanceStatus")
    public abstract MaintenanceParameterEntity toMaintenanceEntity(Vehicle domain);


    // --- Vers DOMAINE ---

    public Vehicle toDomain(VehicleLocalEntity v, FinancialParameterEntity f, MaintenanceParameterEntity m) {
        if (v == null) return null;

        return new Vehicle(
            v.getId(),
            v.getFleetId(),
            v.getCurrentDriverId(),
            v.getVehicleTypeId(),
            null, // licensePlate (Vient du Distant)
            null, // brand (Vient du Distant)
            null, // model (Vient du Distant)
            null, // manufacturingYear (Vient du Distant)
            null, // type label (Vient du Distant)
            null, // color (Vient du Distant)
            v.getStatus(),
            v.getPhotoUrl(),
            mapFinancialToDomain(f),
            mapMaintenanceToDomain(m),
            null  // OperationalParameters
        );
    }

    protected VehicleParameters.Financial mapFinancialToDomain(FinancialParameterEntity f) {
        if (f == null || f.getVehicleId() == null) return null;
        return new VehicleParameters.Financial(
            f.getInsuranceNumber(),
            f.getInsuranceExpiredAt(),
            f.getRegisteredAt(), 
            f.getPurchasedAt(),
            f.getDepreciationRate(),
            f.getCostPerKm()
        );
    }

    protected VehicleParameters.Maintenance mapMaintenanceToDomain(MaintenanceParameterEntity m) {
        if (m == null || m.getVehicleId() == null) return null;
        return new VehicleParameters.Maintenance(
            m.getLastMaintenanceAt(),
            m.getNextMaintenanceAt(),
            m.getEngineStatus(),
            // Conversion Safe String -> Integer pour batteryHealth si nécessaire
            m.getBatteryHealth() != null ? Integer.parseInt(m.getBatteryHealth()) : null,
            m.getMaintenanceStatus()
        );
    }
}