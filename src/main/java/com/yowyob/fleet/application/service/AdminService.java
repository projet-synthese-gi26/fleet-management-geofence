package com.yowyob.fleet.application.service;

import com.yowyob.fleet.domain.exception.AdminException;
import com.yowyob.fleet.domain.ports.in.ManageAdminUseCase;
import com.yowyob.fleet.domain.ports.out.AuthPort;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.UserLocalR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService implements ManageAdminUseCase {

    private final AuthPort authPort;
    private final UserLocalR2dbcRepository userRepo;

    @Override
    public Flux<AuthPort.UserDetail> listFleetManagers(String token) {
        return authPort.getUsersByService("FLEET_MANAGEMENT", token)
                .filter(u -> u.roles().contains("FLEET_MANAGER"))
                .flatMap(this::syncWithLocal);
    }

    @Override
    public Mono<AuthPort.UserDetail> getManagerDetails(UUID managerId, String token, boolean isSuperAdmin) {
        return authPort.getUserById(managerId, token)
                .flatMap(remote -> {
                    if (!isSuperAdmin && remote.roles().contains("FLEET_SUPER_ADMIN")) {
                        return Mono.error(AdminException.masterAccessForbidden());
                    }
                    if (!remote.roles().contains("FLEET_MANAGER")) {
                        return Mono.error(AdminException.actionForbiddenOnUserType());
                    }
                    return syncWithLocal(remote);
                });
    }

    @Override
    public Mono<Void> toggleManagerStatus(UUID managerId, UUID requesterId, boolean isSuperAdmin) {
        return this.getManagerDetails(managerId, "", isSuperAdmin)
                .then(userRepo.findById(managerId))
                .switchIfEmpty(Mono.error(AdminException.managerNotFound()))
                .flatMap(u -> {
                    u.setActive(!u.isActive());
                    u.setNew(false);
                    return userRepo.save(u);
                }).then();
    }

    private Mono<AuthPort.UserDetail> syncWithLocal(AuthPort.UserDetail remote) {
        return userRepo.findById(remote.id())
                .map(local -> new AuthPort.UserDetail(
                        remote.id(), remote.username(), remote.email(), remote.phone(),
                        remote.firstName(), remote.lastName(), remote.service(),
                        remote.roles(), remote.permissions(), local.getPhotoUrl(), 
                        null, null, null))
                .defaultIfEmpty(remote);
    }
}