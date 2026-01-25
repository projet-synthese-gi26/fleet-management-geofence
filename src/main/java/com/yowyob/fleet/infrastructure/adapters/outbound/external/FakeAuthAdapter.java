package com.yowyob.fleet.infrastructure.adapters.outbound.external;

import com.yowyob.fleet.domain.ports.in.AuthUseCase;
import com.yowyob.fleet.domain.ports.out.AuthPort;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * Adaptateur de simulation pour le développement local.
 * Permet de tester l'application sans dépendre du service d'authentification distant.
 */
@Slf4j
public class FakeAuthAdapter implements AuthPort {

    private static final UUID FAKE_ADMIN_ID = UUID.fromString("8a1f5e2c-3d4b-4c6a-9f8e-123456789abc");

    @Override
    public Mono<AuthResponse> login(String identifier, String password) {
        log.info("🛠 MODE FAKE AUTH : Login pour {}", identifier);
        // On simule un admin par défaut pour faciliter les tests
        UserDetail fakeUser = createFakeUser(FAKE_ADMIN_ID, identifier, "fake-admin@yowyob.com", "FLEET_ADMIN");
        return Mono.just(new AuthResponse("fake-access-token", "fake-refresh-token", fakeUser));
    }

    @Override
    public Mono<AuthResponse> registerInRemote(AuthUseCase.RegisterCommand command) {
        log.info("🛠 MODE FAKE AUTH : Inscription pour {}", command.username());
        
        // On génère un ID aléatoire pour le nouvel utilisateur
        UUID newUserId = UUID.randomUUID();
        
        UserDetail newUser = new UserDetail(
            newUserId, 
            command.username(), 
            command.email(), 
            command.phone(), 
            command.firstName(), 
            command.lastName(), 
            "FLEET_MANAGEMENT", 
            command.roles(), 
            List.of("fleet:read", "fleet:write"), // Permissions par défaut
            "https://i.pravatar.cc/150?u=" + newUserId, // Fake photo
            null, null, null // Les données métier (Company, Licence) sont nulles venant de l'Auth
        );
        return Mono.just(new AuthResponse("fake-access-token", "fake-refresh-token", newUser));
    }

    @Override
    public Mono<UserDetail> getUserProfile(String token) {
        log.info("🛠 MODE FAKE AUTH : Récupération du profil courant (me)");
        return Mono.just(createFakeUser(FAKE_ADMIN_ID, "fake_admin", "admin@yowyob.com", "FLEET_ADMIN"));
    }

    @Override
    public Mono<UserDetail> getUserById(UUID userId, String token) {
        log.info("🛠 MODE FAKE AUTH : Récupération user par ID {}", userId);
        // On génère des données dynamiques basées sur l'ID pour que la liste ait l'air réelle
        String suffix = userId.toString().substring(0, 5);
        return Mono.just(createFakeUser(
            userId, 
            "user_" + suffix, 
            "user." + suffix + "@yowyob.test", 
            "FLEET_MANAGER"
        ));
    }

    @Override
    public Flux<UserDetail> getUsersByService(String serviceName, String token) {
        log.info("🛠 MODE FAKE AUTH : Récupération users pour service {}", serviceName);
        // On génère 2 fake managers
        return Flux.just(
            createFakeUser(UUID.randomUUID(), "manager_1", "m1@yowyob.com", "FLEET_MANAGER"),
            createFakeUser(UUID.randomUUID(), "manager_2", "m2@yowyob.com", "FLEET_MANAGER")
        );
    }

    @Override
    public Mono<UserDetail> updateUserProfile(UUID userId, String token, AuthUseCase.UpdateProfileCommand command) {
        log.info("🛠 MODE FAKE AUTH : Update profil pour {}", userId);
        UserDetail updated = new UserDetail(
            userId, 
            "updated_user", 
            command.email(), 
            command.phone(), 
            command.firstName(), 
            command.lastName(), 
            "FLEET_MANAGEMENT", 
            List.of("FLEET_MANAGER"), 
            List.of("*"), 
            "https://i.pravatar.cc/150?u=" + userId,
            null, null, null
        );
        return Mono.just(updated);
    }

    @Override
    public Mono<Void> changePassword(UUID userId, String token, String currentPwd, String newPwd) {
        log.info("🛠 MODE FAKE AUTH : Changement mot de passe pour {}", userId);
        return Mono.empty();
    }

    @Override
    public Mono<Void> deleteRemoteAccount(UUID userId, String token) {
        log.info("🛠 MODE FAKE AUTH : Suppression compte distant pour {}", userId);
        return Mono.empty();
    }

    @Override
    public Mono<Void> updateProfilePicture(UUID userId, String token, AuthUseCase.FileContent file) {
        log.info("🛠 MODE FAKE AUTH : Upload photo pour {} (Fichier: {})", userId, file.filename());
        return Mono.empty();
    }

    @Override
    public Mono<AuthResponse> refresh(String refreshToken) {
        log.info("🛠 MODE FAKE AUTH : Rafraîchissement du token");
        UserDetail fakeUser = createFakeUser(FAKE_ADMIN_ID, "fake_admin", "admin@yowyob.com", "FLEET_ADMIN");
        return Mono.just(new AuthResponse("new-fake-access-token", "new-fake-refresh-token", fakeUser));
    }

    @Override
    public Mono<Boolean> roleExists(String roleName) {
        return Mono.just(true);
    }

    @Override
    public Mono<Void> createRole(String roleName) {
        log.info("🛠 MODE FAKE AUTH : Création rôle {}", roleName);
        return Mono.empty();
    }

    // --- Helpers ---

    private UserDetail createFakeUser(UUID id, String username, String email, String role) {
        return new UserDetail(
            id,
            username,
            email,
            "+237600000000",
            "Fake",
            "User",
            "FLEET_MANAGEMENT",
            List.of(role),
            List.of("fleet:read", "fleet:write", "fleet:admin"),
            "https://i.pravatar.cc/150?u=" + id, // Avatar aléatoire basé sur l'ID
            null, // Company Name (Géré localement)
            null, // Licence (Géré localement)
            null  // Vehicle ID
        );
    }
}