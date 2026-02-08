package com.yowyob.fleet.application.service;

import com.yowyob.fleet.domain.exception.VehicleException;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.FuelTypeEntity;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.ManufacturerEntity;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.FuelTypeR2dbcRepository;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.ManufacturerR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleResourceService {
    private final FuelTypeR2dbcRepository fuelRepo;
    private final ManufacturerR2dbcRepository mfrRepo;

    // --- Manufacturers ---
    public Flux<ManufacturerEntity> getAllManufacturers() { return mfrRepo.findAll(); }
    
    public Mono<ManufacturerEntity> createManufacturer(ManufacturerEntity entity) {
        entity.setId(UUID.randomUUID());
        entity.setNew(true);
        return mfrRepo.save(entity).onErrorResume(e -> Mono.error(VehicleException.duplicateResourceCode()));
    }

    public Mono<Void> deleteManufacturer(UUID id) { return mfrRepo.deleteById(id); }

    // --- Fuel Types ---
    public Flux<FuelTypeEntity> getAllFuelTypes() { return fuelRepo.findAll(); }

    public Mono<FuelTypeEntity> createFuelType(FuelTypeEntity entity) {
        entity.setId(UUID.randomUUID());
        entity.setNew(true);
        return fuelRepo.save(entity).onErrorResume(e -> Mono.error(VehicleException.duplicateResourceCode()));
    }

    public Mono<Void> deleteFuelType(UUID id) { return fuelRepo.deleteById(id); }
}