package com.yowyob.fleet.infrastructure.adapters.outbound.persistence;

import com.yowyob.fleet.domain.model.Vehicle;
import com.yowyob.fleet.domain.ports.out.VehiclePersistencePort;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.FinancialParameterEntity;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.MaintenanceParameterEntity;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.VehicleLocalEntity;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.FinancialParameterR2dbcRepository;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.MaintenanceParameterR2dbcRepository;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.VehicleLocalR2dbcRepository;
import com.yowyob.fleet.infrastructure.mappers.VehicleLocalMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
    public Mono<Vehicle> saveLocalData(Vehicle vehicle) {
        // Préparation de l'entité pivot
        VehicleLocalEntity vEntity = mapper.toVehicleEntity(vehicle);
        
        // On vérifie si c'est une création pour mettre le flag isNew
        return vehicleRepo.findById(vehicle.id())
                .map(existing -> {
                    vEntity.setNew(false);
                    return existing;
                })
                .switchIfEmpty(Mono.defer(() -> {
                    vEntity.setNew(true);
                    return Mono.empty();
                }))
                .then(vehicleRepo.save(vEntity))
                .flatMap(savedV -> {
                    // Initialisation forcée des 1:1 si non existants
                    FinancialParameterEntity fin = mapper.toFinancialEntity(vehicle);
                    fin.setId(fin.getId() == null ? UUID.randomUUID() : fin.getId());
                    fin.setVehicleId(savedV.getId());

                    MaintenanceParameterEntity maint = mapper.toMaintenanceEntity(vehicle);
                    maint.setId(maint.getId() == null ? UUID.randomUUID() : maint.getId());
                    maint.setVehicleId(savedV.getId());

                    return Mono.zip(
                            financialRepo.save(fin),
                            maintenanceRepo.save(maint)
                    ).thenReturn(savedV);
                })
                .map(v -> mapper.toDomain(v, null, null));
    }

    @Override
    public Mono<Vehicle> getLocalDataById(UUID id) {
        return Mono.zip(
                vehicleRepo.findById(id),
                financialRepo.findByVehicleId(id).defaultIfEmpty(new FinancialParameterEntity()),
                maintenanceRepo.findByVehicleId(id).defaultIfEmpty(new MaintenanceParameterEntity())
        ).map(tuple -> mapper.toDomain(tuple.getT1(), tuple.getT2(), tuple.getT3()));
    }

    @Override
    public Mono<Void> deleteLocalData(UUID id) {
        return vehicleRepo.deleteById(id);
    }
}