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

import reactor.core.publisher.Flux;
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
        // 1. On vérifie d'abord si le véhicule existe déjà localement
        return vehicleRepo.existsById(vehicle.id())
                .flatMap(exists -> {
                    VehicleLocalEntity vEntity = mapper.toVehicleEntity(vehicle);
                    
                    // C'EST ICI LE FIX : 
                    // Si existe = false -> isNew = true (Force INSERT)
                    // Si existe = true  -> isNew = false (Fait UPDATE)
                    vEntity.setNew(!exists); 
                    
                    return vehicleRepo.save(vEntity);
                })
                .flatMap(savedV -> {
                    // Gestion des tables 1-1 (Financial & Maintenance)
                    // On utilise une logique similaire ou on force l'ID s'il est null
                    FinancialParameterEntity fin = mapper.toFinancialEntity(vehicle);
                    if (fin.getId() == null) fin.setId(UUID.randomUUID()); 
                    fin.setVehicleId(savedV.getId());

                    MaintenanceParameterEntity maint = mapper.toMaintenanceEntity(vehicle);
                    if (maint.getId() == null) maint.setId(UUID.randomUUID());
                    maint.setVehicleId(savedV.getId());

                    // On sauvegarde les paramètres
                    // Note: Pour être 100% robuste sur les params, on pourrait aussi faire un check, 
                    // mais comme ils sont liés au cycle de vie du véhicule, le save/update cascade souvent bien.
                    return Mono.zip(
                            financialRepo.findByVehicleId(savedV.getId())
                                .map(existing -> { fin.setId(existing.getId()); return fin; })
                                .defaultIfEmpty(fin)
                                .flatMap(financialRepo::save),
                            
                            maintenanceRepo.findByVehicleId(savedV.getId())
                                .map(existing -> { maint.setId(existing.getId()); return maint; })
                                .defaultIfEmpty(maint)
                                .flatMap(maintenanceRepo::save)
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
    
    @Override
    public Flux<Vehicle> getVehiclesByManager(UUID managerId) {
        return vehicleRepo.findByManagerId(managerId)
                .flatMap(v -> Mono.zip(
                    Mono.just(v),
                    financialRepo.findByVehicleId(v.getId()).defaultIfEmpty(new FinancialParameterEntity()),
                    maintenanceRepo.findByVehicleId(v.getId()).defaultIfEmpty(new MaintenanceParameterEntity())
                ))
                .map(tuple -> mapper.toDomain(tuple.getT1(), tuple.getT2(), tuple.getT3()));
    }
}