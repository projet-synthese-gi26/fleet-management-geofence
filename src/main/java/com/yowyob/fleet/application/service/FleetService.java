package com.yowyob.fleet.application.service;

import com.yowyob.fleet.domain.model.Fleet;
import com.yowyob.fleet.domain.ports.in.ManageFleetUseCase;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.FleetEntity;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.FleetR2dbcRepository;
import com.yowyob.fleet.infrastructure.mappers.FleetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FleetService implements ManageFleetUseCase {

    private final FleetR2dbcRepository repository;
    private final FleetMapper mapper;

    @Override
    public Mono<Fleet> createFleet(Fleet fleet, UUID managerId) {
        // 1. Génération de l'ID côté Application (UUID v4)
        UUID newFleetId = UUID.randomUUID();

        Fleet fleetToSave = new Fleet(
                newFleetId, // ID généré ici
                managerId,
                fleet.name(),
                fleet.phoneNumber(),
                Instant.now(),
                0
        );

        // 2. Mapping vers l'entité
        FleetEntity entity = mapper.toEntity(fleetToSave);
        
        // 3. IMPORTANT : Forcer le flag isNew à true pour que R2DBC fasse un INSERT
        // (Sinon il fera un UPDATE car l'ID est non-null)
        entity.setNew(true);

        return repository.save(entity)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Fleet> getFleets(UUID requesterId, boolean isAdmin) {
        if (isAdmin) {
            return repository.findAll().map(mapper::toDomain);
        } else {
            return repository.findAllByManagerId(requesterId).map(mapper::toDomain);
        }
    }

    @Override
    public Mono<Fleet> getFleetById(UUID fleetId, UUID requesterId, boolean isAdmin) {
        return repository.findById(fleetId)
                .map(mapper::toDomain)
                .flatMap(fleet -> {
                    if (!isAdmin && !fleet.managerId().equals(requesterId)) {
                        return Mono.error(new AccessDeniedException("Vous n'avez pas accès à cette flotte."));
                    }
                    return Mono.just(fleet);
                });
    }

    @Override
    public Mono<Fleet> updateFleet(UUID fleetId, Fleet inputFleet, UUID requesterId, boolean isAdmin) {
        return repository.findById(fleetId)
                .flatMap(existingEntity -> {
                    if (!isAdmin && !existingEntity.getManagerId().equals(requesterId)) {
                        return Mono.error(new AccessDeniedException("Modification interdite."));
                    }

                    existingEntity.setName(inputFleet.name());
                    existingEntity.setPhoneNumber(inputFleet.phoneNumber());
                    
                    // Pas besoin de setNew(false), c'est le défaut, donc R2DBC fera un UPDATE
                    return repository.save(existingEntity);
                })
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteFleet(UUID fleetId, UUID requesterId, boolean isAdmin) {
        return repository.findById(fleetId)
                .flatMap(existingEntity -> {
                    if (!isAdmin && !existingEntity.getManagerId().equals(requesterId)) {
                        return Mono.error(new AccessDeniedException("Suppression interdite."));
                    }
                    return repository.delete(existingEntity);
                });
    }
}