package com.yowyob.fleet.infrastructure.adapters.outbound.external;

import com.yowyob.fleet.domain.ports.in.AuthUseCase;
import com.yowyob.fleet.domain.ports.out.AuthPort;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.UUID;

@Slf4j
public class FakeAuthAdapter implements AuthPort {

    @Override
    public Mono<AuthResponse> login(String identifier, String password) {
        log.info("🛠 MODE FAKE AUTH : Login pour {}", identifier);
        UserDetail fakeUser = createFakeUser(identifier, "fake-email@yowyob.com");
        return Mono.just(new AuthResponse("fake-access-token", "fake-refresh-token", fakeUser));
    }

    @Override
    public Mono<AuthResponse> registerInRemote(AuthUseCase.RegisterCommand command) {
        log.info("🛠 MODE FAKE AUTH : Inscription pour {}", command.username());
        UserDetail newUser = new UserDetail(
            UUID.randomUUID(), 
            command.username(), 
            command.email(), 
            command.phone(), 
            command.firstName(), 
            command.lastName(), 
            "FLEET_MANAGEMENT", 
            command.roles(), 
            List.of("*"),
            null, null, null, null // Champs enrichis nuls par défaut
        );
        return Mono.just(new AuthResponse("fake-access-token", "fake-refresh-token", newUser));
    }

    @Override
    public Mono<UserDetail> getUserProfile(String token) {
        log.info("🛠 MODE FAKE AUTH : Récupération du profil (me)");
        return Mono.just(createFakeUser("fake_admin", "admin@yowyob.com"));
    }

    @Override
    public Mono<AuthResponse> refresh(String refreshToken) {
        log.info("🛠 MODE FAKE AUTH : Rafraîchissement du token");
        UserDetail fakeUser = createFakeUser("fake_admin", "admin@yowyob.com");
        return Mono.just(new AuthResponse("new-fake-access-token", "new-fake-refresh-token", fakeUser));
    }

    @Override
    public Mono<Boolean> roleExists(String roleName) {
        return Mono.just(true);
    }

    @Override
    public Mono<Void> createRole(String roleName) {
        return Mono.empty();
    }

    @Override 
    public Mono<UserDetail> updateUserProfile(UUID userId, String token, AuthUseCase.UpdateProfileCommand command) { 
        log.info("🛠 MODE FAKE AUTH : Update profil pour {}", userId);
        return Mono.just(createFakeUser("fake_admin", command.email()));
    }
    
    @Override 
    public Mono<Void> changePassword(UUID userId, String token, String currentPwd, String newPwd) { 
        log.info("🛠 MODE FAKE AUTH : Changement mot de passe pour {}", userId);
        return Mono.empty(); 
    }
    
    @Override 
    public Mono<Void> deleteRemoteAccount(UUID userId, String token) { 
        log.info("🛠 MODE FAKE AUTH : Suppression compte pour {}", userId);
        return Mono.empty(); 
    }

    @Override
    public Mono<Void> updateProfilePicture(UUID userId, String token, AuthUseCase.FileContent file) {
        log.info("🛠 MODE FAKE AUTH : Upload photo pour {} (Fichier: {})", userId, file.filename());
        return Mono.empty();
    }

    private UserDetail createFakeUser(String username, String email) {
        return new UserDetail(
            UUID.fromString("8a1f5e2c-3d4b-4c6a-9f8e-123456789abc"),
            username,
            email,
            "+237600000000",
            "Fake",
            "User",
            "FLEET_MANAGEMENT",
            List.of("FLEET_MANAGER"),
            List.of("fleet:read", "fleet:write"),
            null, null, null, null
        );
    }
}