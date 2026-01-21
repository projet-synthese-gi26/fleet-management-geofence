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
        // Attention: le mapper génère une entité simple. 
        // Si c'est une création, on doit forcer isNew=true manuellement si le mapper ne le fait pas.
        DriverEntity entity = mapper.toEntity(driver);
        
        // Petite astuce: Si on sait qu'on appelle cette méthode pour créer, on peut vérifier si l'ID existe déjà
        // Mais pour l'instant, supposons que save() gère les mises à jour et les créations.
        // Dans notre UseCase actuel (Register), c'est une création.
        
        // Pour être propre, il faudrait que le service domaine indique s'il s'agit d'une création.
        // Hack rapide et robuste : on essaie de trouver, si vide -> insert (isNew=true), sinon update.
        
        return repository.findById(driver.userId())
                .map(existing -> {
                    // C'est un update
                    existing.setLicenceNumber(driver.licenceNumber());
                    existing.setStatus(driver.status());
                    existing.setAssignedVehicleId(driver.assignedVehicleId());
                    return existing;
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // C'est une création
                    DriverEntity newEntity = mapper.toEntity(driver);
                    newEntity.setNewRecord(true); // Setter lombok généré ou méthode manuelle
                    return Mono.just(newEntity);
                }))
                .flatMap(repository::save)
                .map(mapper::toDomain);
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