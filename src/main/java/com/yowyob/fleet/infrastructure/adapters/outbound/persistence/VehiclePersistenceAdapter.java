package com.yowyob.fleet.infrastructure.adapters.outbound.persistence;

import com.yowyob.fleet.domain.model.Vehicle;
import com.yowyob.fleet.domain.ports.out.VehiclePersistencePort;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.FinancialParameterEntity;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.MaintenanceParameterEntity;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.FinancialParameterR2dbcRepository;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.MaintenanceParameterR2dbcRepository;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.VehicleLocalR2dbcRepository;
import com.yowyob.fleet.infrastructure.mappers.VehicleLocalMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VehiclePersistenceAdapter implements VehiclePersistencePort {

    private final VehicleLocalR2dbcRepository vehicleRepo;
    private final FinancialParameterR2dbcRepository financialRepo;
    private final MaintenanceParameterR2dbcRepository maintenanceRepo;
    private final VehicleLocalMapper mapper;

    @Override
    public Mono<Vehicle> saveLocalData(Vehicle vehicle) {
        // 1. Save the main vehicle pivot first
        return vehicleRepo.save(mapper.toVehicleEntity(vehicle))
                .flatMap(savedVehicle -> {
                    // 2. Map parameters to entities
                    var financialEntity = mapper.toFinancialEntity(vehicle);
                    var maintenanceEntity = mapper.toMaintenanceEntity(vehicle);
                    
                    // Ensure the foreign key is set
                    financialEntity.setVehicleId(savedVehicle.getId());
                    maintenanceEntity.setVehicleId(savedVehicle.getId());

                    // 3. Save parameters in parallel using zip
                    return Mono.zip(
                            financialRepo.save(financialEntity),
                            maintenanceRepo.save(maintenanceEntity)
                    ).thenReturn(savedVehicle);
                })
                // 4. Return the domain object (we can fetch again or map directly)
                .map(v -> mapper.toDomain(v, null, null));
    }

    @Override
    public Mono<Vehicle> getLocalDataById(UUID id) {
        // Zip: fetch all 3 local tables in parallel
        return Mono.zip(
                vehicleRepo.findById(id),
                financialRepo.findByVehicleId(id).defaultIfEmpty(new FinancialParameterEntity()),
                maintenanceRepo.findByVehicleId(id).defaultIfEmpty(new MaintenanceParameterEntity())
        ).map(tuple -> mapper.toDomain(tuple.getT1(), tuple.getT2(), tuple.getT3()));
    }

    @Override
    public Mono<Void> deleteLocalData(UUID id) {
        // Delete pivot - the CASCADE in SQL will handle the parameters
        return vehicleRepo.deleteById(id);
    }
}