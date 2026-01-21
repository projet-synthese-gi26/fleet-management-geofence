package com.yowyob.fleet.application.service;

import com.yowyob.fleet.domain.ports.in.AuthUseCase;
import com.yowyob.fleet.domain.ports.out.AuthPort;
import com.yowyob.fleet.domain.ports.out.DriverPersistencePort;
import com.yowyob.fleet.domain.ports.out.FleetManagerPersistencePort;
import com.yowyob.fleet.domain.model.Driver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

    private final AuthPort authPort;
    private final DriverPersistencePort driverPort;
    private final FleetManagerPersistencePort managerPort;

    @Override
    public Mono<AuthPort.AuthResponse> login(String identifier, String password) {
        return authPort.login(identifier, password)
                .flatMap(response -> syncLocalProfile(response.user()).thenReturn(response));
    }

    @Override
    public Mono<AuthPort.AuthResponse> register(RegisterCommand command) {
        return ensureRolesExist(command.roles())
                .then(authPort.registerInRemote(command))
                .flatMap(response -> createLocalProfile(response.user()).thenReturn(response));
    }

    @Override
    public Mono<AuthPort.AuthResponse> refreshToken(String refreshToken) {
        return authPort.refresh(refreshToken);
    }

    @Override
    public Mono<AuthPort.UserDetail> me(String token) {
        return authPort.getUserProfile(token)
                .flatMap(this::enrichWithLocalData);
    }

    @Override
    public Mono<AuthPort.UserDetail> updateProfile(UUID userId, String token, UpdateProfileCommand command) {
        return authPort.updateUserProfile(userId, token, command)
                .flatMap(updatedUser -> {
                    Mono<Void> localUpdate = Mono.empty();
                    if (updatedUser.roles().contains("FLEET_MANAGER") && command.companyName() != null) {
                        localUpdate = managerPort.updateCompany(userId, command.companyName());
                    } 
                    // Pour le driver, on pourrait update le permis ici si nécessaire
                    
                    return localUpdate.then(enrichWithLocalData(updatedUser));
                });
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
        // 1. Supprimer côté Auth distant
        return authPort.deleteRemoteAccount(userId, token)
            .then(Mono.defer(() -> {
                // 2. Supprimer localement (Cascade DB fera le reste si FK configurées, 
                // mais on peut expliciter pour plus de sûreté ou pour les fichiers liés)
                // Ici on assume que le ON DELETE CASCADE de la DB fait le job sur drivers/fleet_managers
                return Mono.empty();
            }));
    }

    // --- LOGIQUE INTERNE ---
    
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
            Driver driver = new Driver(user.id(), tempPermit, true, null);
            return driverPort.save(driver).then();
        }
        return Mono.empty();
    }

    private Mono<Void> syncLocalProfile(AuthPort.UserDetail user) {
        return createLocalProfile(user).onErrorResume(e -> Mono.empty());
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