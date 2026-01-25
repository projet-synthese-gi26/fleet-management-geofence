package com.yowyob.fleet.application.service;

import com.yowyob.fleet.domain.ports.in.ManageFleetManagerUseCase;
import com.yowyob.fleet.domain.ports.out.AuthPort;
import com.yowyob.fleet.domain.ports.out.FleetManagerPersistencePort;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.FleetManagerResponse;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.FleetManagerR2dbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FleetManagerService implements ManageFleetManagerUseCase {

    private final FleetManagerR2dbcRepository managerRepository;
    private final FleetManagerPersistencePort managerPersistencePort;
    private final AuthPort authPort;

    private static final String SERVICE_NAME = "FLEET_MANAGEMENT";
    private static final String ROLE_MANAGER = "FLEET_MANAGER";

    @Override
    public Flux<FleetManagerResponse> getAllManagers(String token) {
        // 1. Source de Vérité = Auth Service
        return authPort.getUsersByService(SERVICE_NAME, token)
            // 2. Filtrage des Managers uniquement
            .filter(user -> user.roles() != null && user.roles().contains(ROLE_MANAGER))
            // 3. Synchronisation & Enrichissement Local
            .flatMap(remoteUser -> syncAndEnrich(remoteUser));
    }

    @Override
    public Mono<FleetManagerResponse> getManagerDetails(UUID userId, String token) {
        // Source de vérité = Auth Service (Vérifie que l'user existe toujours)
        return authPort.getUserById(userId, token)
            .flatMap(this::syncAndEnrich);
    }

    /**
     * Logique de Synchronisation :
     * - Cherche le profil local.
     * - Si inexistant -> Le crée (Auto-Repair).
     * - Retourne le DTO combiné.
     */
    private Mono<FleetManagerResponse> syncAndEnrich(AuthPort.UserDetail remoteUser) {
        return managerRepository.findById(remoteUser.id())
            .switchIfEmpty(
                // Si pas de profil local, on le crée à la volée
                managerPersistencePort.createProfile(remoteUser.id(), "Société de " + remoteUser.lastName())
                    .then(managerRepository.findById(remoteUser.id()))
            )
            .map(localEntity -> new FleetManagerResponse(
                remoteUser.id(),
                remoteUser.firstName(),
                remoteUser.lastName(),
                remoteUser.email(),
                remoteUser.phone(),
                localEntity.getCompanyName(), // Donnée locale
                "ACTIVE", // TODO: Mapper le statut réel si dispo
                0, // Stats à implémenter plus tard
                remoteUser.photoUrl()
            ));
    }

    @Override
    public Mono<Void> updateManagerCompany(UUID userId, String companyName) {
        return managerPersistencePort.updateCompany(userId, companyName);
    }

    @Override
    public Mono<Void> deleteManager(UUID userId, String token) {
        log.info("Suppression Manager {} : Distant puis Local", userId);
        return authPort.deleteRemoteAccount(userId, token)
                .then(managerRepository.deleteById(userId));
    }
}