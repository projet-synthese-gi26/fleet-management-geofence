package com.yowyob.fleet.infrastructure.mappers;

import com.yowyob.fleet.domain.model.Vehicle;
import com.yowyob.fleet.domain.model.VehicleParameters;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public abstract class VehicleLocalMapper {

    // --- Vers ENTITY (Pivot principal) ---

    @Mapping(target = "id", source = "id")
    @Mapping(target = "fleetId", source = "fleetId")
    @Mapping(target = "currentDriverId", source = "currentDriverId")
    @Mapping(target = "vehicleTypeId", source = "vehicleTypeId")
    @Mapping(target = "licensePlate", source = "licensePlate")
    @Mapping(target = "brand", source = "brand")
    @Mapping(target = "model", source = "model")
    @Mapping(target = "manufacturingYear", source = "manufacturingYear")
    @Mapping(target = "color", source = "color")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "photoUrl", source = "photoUrl")
    @Mapping(target = "new", ignore = true) // Géré manuellement dans l'adapter
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


    // --- Vers DOMAINE (Assemblage manuel des 3 sources de données locales) ---

    public Vehicle toDomain(VehicleLocalEntity v, FinancialParameterEntity f, MaintenanceParameterEntity m) {
        if (v == null) return null;

        return new Vehicle(
            v.getId(),
            v.getFleetId(),
            v.getCurrentDriverId(),
            v.getVehicleTypeId(),
            v.getLicensePlate(),
            v.getBrand(),
            v.getModel(),
            v.getManufacturingYear(),
            null, // Le 'type' (label String) est fourni par l'API externe ou un service de dictionnaire
            v.getColor(),
            v.getStatus(),
            v.getPhotoUrl(),
            mapFinancialToDomain(f),
            mapMaintenanceToDomain(m),
            null  // OperationalParameters gérés séparément (temps réel)
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
            m.getBatteryHealth(),
            m.getMaintenanceStatus()
        );
    }
}