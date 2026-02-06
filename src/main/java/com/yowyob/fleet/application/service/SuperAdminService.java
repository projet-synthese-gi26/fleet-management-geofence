package com.yowyob.fleet.application.service;

import com.yowyob.fleet.domain.exception.SuperAdminException;
import com.yowyob.fleet.domain.ports.in.AuthUseCase;
import com.yowyob.fleet.domain.ports.in.ManageSuperAdminUseCase;
import com.yowyob.fleet.domain.ports.out.AuthPort;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.UserLocalR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SuperAdminService implements ManageSuperAdminUseCase {

    private final AuthPort authPort;
    private final UserLocalR2dbcRepository userRepo;

    @Override
    public Mono<AuthPort.AuthResponse> createAdmin(AuthUseCase.RegisterCommand command) {
        AuthUseCase.RegisterCommand adminCmd = new AuthUseCase.RegisterCommand(
            command.username(), command.password(), command.email(), command.phone(),
            command.firstName(), command.lastName(), List.of("FLEET_ADMIN"), null
        );
        return authPort.registerInRemote(adminCmd);
    }

    @Override
    public Flux<AuthPort.UserDetail> listAdmins(String token) {
        return authPort.getUsersByService("FLEET_MANAGEMENT", token)
                .filter(u -> u.roles().contains("FLEET_ADMIN"))
                .flatMap(this::syncWithLocal);
    }

    @Override
    public Mono<AuthPort.UserDetail> getAdminDetails(UUID adminId, String token) {
        return authPort.getUserById(adminId, token)
                .filter(u -> u.roles().contains("FLEET_ADMIN"))
                .switchIfEmpty(Mono.error(SuperAdminException.roleMismatch()))
                .flatMap(this::syncWithLocal);
    }

    @Override
    public Mono<Void> toggleAdminStatus(UUID adminId, UUID requesterId) {
        if (adminId.equals(requesterId)) return Mono.error(SuperAdminException.selfActionForbidden());
        
        return userRepo.findById(adminId)
                .switchIfEmpty(Mono.error(SuperAdminException.adminNotFound()))
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