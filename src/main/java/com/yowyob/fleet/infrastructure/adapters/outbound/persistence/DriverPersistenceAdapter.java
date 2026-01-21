package com.yowyob.fleet.infrastructure.adapters.outbound.persistence;
import com.yowyob.fleet.domain.model.Driver;
import com.yowyob.fleet.domain.ports.out.DriverPersistencePort;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.DriverEntity;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.DriverR2dbcRepository;
import com.yowyob.fleet.infrastructure.mappers.DriverMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DriverPersistenceAdapter implements DriverPersistencePort {

    private final DriverR2dbcRepository repository;
    private final DriverMapper mapper;

    @Override
public Mono<Driver> save(Driver driver) {
    DriverEntity entity = mapper.toEntity(driver);
    entity.setNewRecord(true); // Utilisation de setNewRecord
    return repository.save(entity).map(mapper::toDomain);
}

@Override
public Mono<Void> updateVehicleAssignment(UUID userId, UUID vehicleId) {
    return repository.findById(userId)
            .flatMap(entity -> {
                entity.setAssignedVehicleId(vehicleId);
                entity.setNewRecord(false); // Ce n'est pas un nouvel enregistrement
                return repository.save(entity);
            }).then();
}

    @Override
    public Mono<Driver> findById(UUID userId) {
        return repository.findById(userId).map(mapper::toDomain);
    }

    @Override
    public Flux<Driver> findAllByFleetId(UUID fleetId) {
        return repository.findByFleetId(fleetId).map(mapper::toDomain);
    }

}