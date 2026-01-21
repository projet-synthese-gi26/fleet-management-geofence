package com.yowyob.fleet.domain.ports.out;

import com.yowyob.fleet.domain.ports.in.AuthUseCase;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.UUID;

public interface AuthPort {
    // Auth pure
    Mono<AuthResponse> login(String identifier, String password);
    Mono<AuthResponse> refresh(String refreshToken);

    // Gestion Utilisateur Distant
    Mono<AuthResponse> registerInRemote(AuthUseCase.RegisterCommand command);
    Mono<UserDetail> getUserProfile(String token);
    Mono<UserDetail> updateUserProfile(UUID userId, String token, AuthUseCase.UpdateProfileCommand command);
    Mono<Void> changePassword(UUID userId, String token, String currentPwd, String newPwd);
    Mono<Void> deleteRemoteAccount(UUID userId, String token);
    Mono<Void> updateProfilePicture(UUID userId, String token, AuthUseCase.FileContent file); // NEW
    
    // Gestion Rôles Distants
    Mono<Boolean> roleExists(String roleName);
    Mono<Void> createRole(String roleName);

    // Objets de réponse (Outputs)
    record AuthResponse(String accessToken, String refreshToken, UserDetail user) {}

    record UserDetail(
        UUID id, 
        String username, 
        String email,
        String phone,
        String firstName,
        String lastName,
        String service,
        List<String> roles, 
        List<String> permissions,
        String photoUrl,
        String companyName,
        String licenceNumber,
        String vehicleId
    ) {}
}