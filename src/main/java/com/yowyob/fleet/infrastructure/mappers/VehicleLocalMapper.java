package com.yowyob.fleet.infrastructure.mappers;

import com.yowyob.fleet.domain.model.Vehicle;
import com.yowyob.fleet.domain.model.VehicleParameters;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VehicleLocalMapper {

    // Mapping to Pivot Table (vehicles)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "fleetId", source = "fleetId")
    @Mapping(target = "driverId", ignore = true) // Will be handled during assignment task
    @Mapping(target = "userId", ignore = true)   // Will be handled during assignment task
    VehicleLocalEntity toVehicleEntity(Vehicle domain);

    // Mapping to Financial Table
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicleId", source = "id")
    @Mapping(target = "insuranceNumber", source = "financialParameters.insuranceNumber") // Added
    @Mapping(target = "insuranceExpiredAt", source = "financialParameters.insuranceExpiryDate")
    @Mapping(target = "registeredAt", source = "financialParameters.registrationDate")
    @Mapping(target = "purchasedAt", source = "financialParameters.purchaseDate")
    @Mapping(target = "depreciationRate", source = "financialParameters.depreciationRate")
    @Mapping(target = "costPerKm", source = "financialParameters.costPerKm")
    FinancialParameterEntity toFinancialEntity(Vehicle domain);

    // Mapping to Maintenance Table
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicleId", source = "id")
    @Mapping(target = "lastMaintenanceAt", source = "maintenanceParameters.lastMaintenanceDate")
    @Mapping(target = "nextMaintenanceAt", source = "maintenanceParameters.nextMaintenanceDue")
    @Mapping(target = "engineStatus", source = "maintenanceParameters.engineStatus") // Added
    @Mapping(target = "batteryHealth", source = "maintenanceParameters.batteryHealth") // Added
    @Mapping(target = "maintenanceStatus", source = "maintenanceParameters.maintenanceStatus") // Added
    MaintenanceParameterEntity toMaintenanceEntity(Vehicle domain);

    // Domain reconstruction (Helper)
    default Vehicle toDomain(VehicleLocalEntity v, FinancialParameterEntity f, MaintenanceParameterEntity m) {
        if (v == null) return null;
        return new Vehicle(
            v.getId(),
            v.getFleetId(),
            null, null, null, null, null, null, // Remote data
            f == null ? null : new VehicleParameters.Financial(
                f.getInsuranceNumber(), f.getInsuranceExpiredAt(), f.getRegisteredAt(), f.getPurchasedAt(), f.getDepreciationRate(), f.getCostPerKm()
            ),
            m == null ? null : new VehicleParameters.Maintenance(
                m.getLastMaintenanceAt(), m.getNextMaintenanceAt(), m.getEngineStatus(), 
                m.getBatteryHealth() != null ? Integer.parseInt(m.getBatteryHealth()) : null, 
                m.getMaintenanceStatus()
            ),
            null
        );
    }
}