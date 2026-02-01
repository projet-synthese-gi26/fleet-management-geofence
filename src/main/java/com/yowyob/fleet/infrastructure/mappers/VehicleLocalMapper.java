package com.yowyob.fleet.infrastructure.mappers;

import com.yowyob.fleet.domain.model.Vehicle;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class VehicleLocalMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "fleetId", source = "fleetId")
    @Mapping(target = "managerId", source = "managerId")
    @Mapping(target = "currentDriverId", source = "currentDriverId")
    @Mapping(target = "vehicleTypeId", source = "vehicleTypeId")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "photoUrl", source = "photoUrl")
    
    // Ajouts explicites pour garantir le transfert des données
    @Mapping(target = "licensePlate", source = "licensePlate")
    @Mapping(target = "brand", source = "brand")
    @Mapping(target = "model", source = "model")
    @Mapping(target = "manufacturingYear", source = "manufacturingYear")
    @Mapping(target = "color", source = "color")
    
    @Mapping(target = "new", ignore = true) 
    public abstract VehicleLocalEntity toVehicleEntity(Vehicle domain);

    // ... Le reste du fichier reste inchangé (méthodes toDomain, toFinancialEntity, etc.)
    // Je ne remets pas tout pour ne pas encombrer, garde le reste tel quel.
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicleId", source = "id")
    @Mapping(target = "insuranceNumber", source = "financialParameters.insuranceNumber")
    @Mapping(target = "insuranceExpiredAt", source = "financialParameters.insuranceExpiryDate")
    @Mapping(target = "registeredAt", source = "financialParameters.registrationDate")
    @Mapping(target = "purchasedAt", source = "financialParameters.purchaseDate")
    @Mapping(target = "depreciationRate", source = "financialParameters.depreciationRate")
    @Mapping(target = "costPerKm", source = "financialParameters.costPerKm")
    public abstract FinancialParameterEntity toFinancialEntity(Vehicle domain);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicleId", source = "id")
    @Mapping(target = "lastMaintenanceAt", source = "maintenanceParameters.lastMaintenanceDate")
    @Mapping(target = "nextMaintenanceAt", source = "maintenanceParameters.nextMaintenanceDue")
    @Mapping(target = "engineStatus", source = "maintenanceParameters.engineStatus")
    @Mapping(target = "batteryHealth", source = "maintenanceParameters.batteryHealth")
    @Mapping(target = "maintenanceStatus", source = "maintenanceParameters.maintenanceStatus")
    public abstract MaintenanceParameterEntity toMaintenanceEntity(Vehicle domain);

    public Vehicle toDomain(VehicleLocalEntity v, FinancialParameterEntity f, MaintenanceParameterEntity m) {
        if (v == null) return null;

        return new Vehicle(
            v.getId(),
            v.getFleetId(),
            v.getManagerId(),
            v.getCurrentDriverId(),
            v.getVehicleTypeId(),
            v.getLicensePlate(), // On lit bien depuis l'entité locale maintenant
            null, // vin (pas stocké localement dans cette version)
            v.getBrand(),
            v.getModel(),
            v.getManufacturingYear(),
            null, // transmission
            null, // fuel
            null, // tank
            null, // seats
            null, // consumption
            v.getColor(),
            v.getStatus(),
            v.getPhotoUrl(),
            null, // vin photo
            null, // reg photo
            mapFinancialToDomain(f),
            mapMaintenanceToDomain(m),
            null
        );
    }
    
    // Garde les méthodes protected mapFinancialToDomain et mapMaintenanceToDomain telles quelles
    protected com.yowyob.fleet.domain.model.VehicleParameters.Financial mapFinancialToDomain(FinancialParameterEntity f) {
        if (f == null || f.getVehicleId() == null) return null;
        return new com.yowyob.fleet.domain.model.VehicleParameters.Financial(
            f.getInsuranceNumber(),
            f.getInsuranceExpiredAt(),
            f.getRegisteredAt(), 
            f.getPurchasedAt(),
            f.getDepreciationRate(),
            f.getCostPerKm()
        );
    }

    protected com.yowyob.fleet.domain.model.VehicleParameters.Maintenance mapMaintenanceToDomain(MaintenanceParameterEntity m) {
        if (m == null || m.getVehicleId() == null) return null;
        return new com.yowyob.fleet.domain.model.VehicleParameters.Maintenance(
            m.getLastMaintenanceAt(),
            m.getNextMaintenanceAt(),
            m.getEngineStatus(),
            m.getBatteryHealth() != null ? Integer.parseInt(m.getBatteryHealth()) : null,
            m.getMaintenanceStatus()
        );
    }
}