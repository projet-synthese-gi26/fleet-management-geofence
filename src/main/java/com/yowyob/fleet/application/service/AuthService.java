package com.yowyob.fleet.application.service;

import com.yowyob.fleet.domain.ports.in.AuthUseCase;
import com.yowyob.fleet.domain.ports.out.AuthPort;
import com.yowyob.fleet.domain.ports.out.DriverPersistencePort;
import com.yowyob.fleet.domain.ports.out.FleetManagerPersistencePort;
import com.yowyob.fleet.domain.exception.AuthException;
import com.yowyob.fleet.domain.model.Driver;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.UserLocalEntity;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.UserLocalR2dbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

    private final AuthPort authPort;
    private final UserLocalR2dbcRepository userRepo;
    private final DriverPersistencePort driverPort;
    private final FleetManagerPersistencePort managerPort;

    @Override
    public Mono<AuthPort.AuthResponse> login(String identifier, String password) {
        return authPort.login(identifier, password)
                .flatMap(response -> pullSyncLocalUser(response.user())
                        .then(checkUserAccess(response.user().id()))
                        .thenReturn(response));
    }

    @Override
    public Mono<AuthPort.AuthResponse> register(RegisterCommand command) {
        return ensureRolesExist(command.roles())
                .then(authPort.registerInRemote(command))
                .flatMap(response -> createLocalProfile(response.user())
                        .then(pullSyncLocalUser(response.user()))
                        .thenReturn(response));
    }

    @Override
    public Mono<AuthPort.AuthResponse> refreshToken(String refreshToken) {
        return authPort.refresh(refreshToken)
                .flatMap(response -> pullSyncLocalUser(response.user())
                        .then(checkUserAccess(response.user().id()))
                        .thenReturn(response));
    }

    @Override
    public Mono<AuthPort.UserDetail> me(String token) {
        return authPort.getUserProfile(token)
                .flatMap(remote -> pullSyncLocalUser(remote).thenReturn(remote))
                .flatMap(this::enrichWithLocalData);
    }

    /**
     * PULL SYNC : Met à jour le cache local fleet.users depuis les données distantes.
     */
    private Mono<Void> pullSyncLocalUser(AuthPort.UserDetail remote) {
        return userRepo.findById(remote.id())
                .flatMap(local -> {
                    local.setUsername(remote.username());
                    local.setEmail(remote.email());
                    local.setFirstName(remote.firstName());
                    local.setLastName(remote.lastName());
                    local.setPhotoUrl(remote.photoUrl());
                    local.setLastLoginAt(Instant.now());
                    local.setNew(false);
                    return userRepo.save(local);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    UserLocalEntity newLocal = UserLocalEntity.builder()
                            .id(remote.id())
                            .username(remote.username())
                            .email(remote.email())
                            .firstName(remote.firstName())
                            .lastName(remote.lastName())
                            .photoUrl(remote.photoUrl())
                            .isActive(true) // Par défaut actif à la première sync
                            .lastLoginAt(Instant.now())
                            .build();
                    newLocal.setNew(true);
                    return userRepo.save(newLocal);
                }))
                .then();
    }

    /**
     * VERROU LOCAL : Vérifie si l'utilisateur n'est pas banni ou supprimé en local.
     */
/**
     * VERROU LOCAL : Vérifie si l'utilisateur n'est pas banni ou supprimé en local.
     */
    private Mono<Void> checkUserAccess(UUID userId) {
        return userRepo.findById(userId)
                .flatMap(user -> {
                    // Utilisation de nos exceptions modulaires au lieu de ResponseStatusException
                    if (user.getDeletedAt() != null) {
                        return Mono.error(AuthException.accountDeleted()); // Lance AUTH_003
                    }
                    if (!user.isActive()) {
                        return Mono.error(AuthException.accountLocked());  // Lance AUTH_002
                    }
                    return Mono.empty();
                });
    }

    @Override
    public Mono<AuthPort.UserDetail> updateProfile(UUID userId, String token, UpdateProfileCommand command) {
        return authPort.updateUserProfile(userId, token, command)
                .flatMap(remote -> pullSyncLocalUser(remote).thenReturn(remote))
                .flatMap(this::enrichWithLocalData);
    }

    @Override
    public Mono<Void> changePassword(UUID userId, String token, String currentPwd, String newPwd) {
        return authPort.changePassword(userId, token, currentPwd, newPwd);
    }

    @Override
    public Mono<Void> updateProfilePicture(UUID userId, String token, FileContent file) {
        return authPort.updateProfilePicture(userId, token, file);
    }

    @Override
    public Mono<Void> deleteAccount(UUID userId, String token) {
        // SOFT DELETE LOCAL
        return userRepo.findById(userId)
                .flatMap(user -> {
                    user.setDeletedAt(Instant.now());
                    user.setActive(false);
                    user.setNew(false);
                    return userRepo.save(user);
                })
                .then(authPort.moveUserToService(userId, "USER_DELETED", token));
    }

    // --- HELPERS EXISTANTS ---
    
    private Mono<Void> ensureRolesExist(List<String> roles) {
        return Flux.fromIterable(roles)
                .flatMap(role -> authPort.roleExists(role)
                        .flatMap(exists -> !Boolean.TRUE.equals(exists) ? authPort.createRole(role) : Mono.empty())
                ).then();
    }

    private Mono<Void> createLocalProfile(AuthPort.UserDetail user) {
        if (user.roles().contains("FLEET_MANAGER")) {
            return managerPort.createProfile(user.id(), null);
        } else if (user.roles().contains("FLEET_DRIVER")) {
            String tempPermit = "PENDING-" + user.id().toString().substring(0, 8);
            Driver driver = new Driver(user.id(), null, tempPermit, "ACTIVE", null, "");
            return driverPort.save(driver).then();
        }
        return Mono.empty();
    }

    private Mono<AuthPort.UserDetail> enrichWithLocalData(AuthPort.UserDetail remoteUser) {
        if (remoteUser.roles().contains("FLEET_MANAGER")) {
            return managerPort.getCompanyName(remoteUser.id())
                    .map(company -> new AuthPort.UserDetail(
                            remoteUser.id(), remoteUser.username(), remoteUser.email(), remoteUser.phone(),
                            remoteUser.firstName(), remoteUser.lastName(), remoteUser.service(),
                            remoteUser.roles(), remoteUser.permissions(), remoteUser.photoUrl(),
                            company, null, null
                    ))
                    .defaultIfEmpty(remoteUser);
        } else if (remoteUser.roles().contains("FLEET_DRIVER")) {
            return driverPort.findById(remoteUser.id())
                    .map(driver -> new AuthPort.UserDetail(
                            remoteUser.id(), remoteUser.username(), remoteUser.email(), remoteUser.phone(),
                            remoteUser.firstName(), remoteUser.lastName(), remoteUser.service(),
                            remoteUser.roles(), remoteUser.permissions(), remoteUser.photoUrl(),
                            null, driver.licenceNumber(), driver.assignedVehicleId() != null ? driver.assignedVehicleId().toString() : null
                    ))
                    .defaultIfEmpty(remoteUser);
        }
        return Mono.just(remoteUser);
    }
}