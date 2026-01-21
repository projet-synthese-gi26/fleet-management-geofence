package com.yowyob.fleet.infrastructure.adapters.outbound.external;

import com.yowyob.fleet.domain.ports.in.AuthUseCase;
import com.yowyob.fleet.domain.ports.out.AuthPort;
import com.yowyob.fleet.infrastructure.adapters.outbound.external.client.AuthApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class RemoteAuthAdapter implements AuthPort {

    private final AuthApiClient authApiClient;
    private final WebClient.Builder webClientBuilder;
    
    @Value("${application.auth.url}")
    private String authServiceUrl;

    private static final String SERVICE_NAME = "FLEET_MANAGEMENT";

    @Override
    public Mono<AuthResponse> login(String identifier, String password) {
        return authApiClient.authenticate(new AuthApiClient.LoginRequest(identifier, password))
                .map(this::mapToAuthResponse);
    }

    @Override
    public Mono<AuthResponse> registerInRemote(AuthUseCase.RegisterCommand command) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        
        AuthApiClient.RegisterRequest registerRequest = new AuthApiClient.RegisterRequest(
            command.username(), command.password(), command.email(), 
            command.phone(), command.firstName(), command.lastName(), 
            SERVICE_NAME, command.roles()
        );
        
        // CORRECTION ICI : "user" -> "data" pour matcher le Swagger distant
        builder.part("data", registerRequest);

        if (command.photo() != null && command.photo().data() != null) {
            builder.part("file", command.photo().data())
                   .filename(command.photo().filename())
                   .header("Content-Type", command.photo().contentType());
        }

        return webClientBuilder.build()
                .post()
                .uri(authServiceUrl + "/api/auth/register")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(builder.build())
                .retrieve()
                .bodyToMono(AuthApiClient.TraMaSysResponse.class)
                .map(this::mapToAuthResponse)
                .doOnError(e -> log.error("Erreur Register Distant : {}", e.getMessage()));
    }

    @Override
    public Mono<UserDetail> getUserProfile(String token) {
        return authApiClient.getCurrentUser(ensureBearer(token))
                .map(this::mapToDomainUserDetail);
    }

    @Override
    public Mono<UserDetail> updateUserProfile(UUID userId, String token, AuthUseCase.UpdateProfileCommand command) {
        AuthApiClient.UpdateUserRequest req = new AuthApiClient.UpdateUserRequest(
            command.firstName(), command.lastName(), command.phone(), command.email()
        );
        
        return webClientBuilder.build()
                .put()
                .uri(authServiceUrl + "/api/users/" + userId)
                .header("Authorization", ensureBearer(token))
                .bodyValue(req)
                .retrieve()
                .bodyToMono(AuthApiClient.UserDetailResponse.class)
                .map(this::mapToDomainUserDetail);
    }

    @Override
    public Mono<Void> changePassword(UUID userId, String token, String currentPwd, String newPwd) {
        AuthApiClient.ChangePasswordRequest req = new AuthApiClient.ChangePasswordRequest(currentPwd, newPwd);
        
        return webClientBuilder.build()
                .put()
                .uri(authServiceUrl + "/api/users/" + userId + "/password")
                .header("Authorization", ensureBearer(token))
                .bodyValue(req)
                .retrieve()
                .bodyToMono(Void.class);
    }

    @Override
    public Mono<Void> updateProfilePicture(UUID userId, String token, AuthUseCase.FileContent file) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file.data())
               .filename(file.filename())
               .header("Content-Type", file.contentType());

        return webClientBuilder.build()
                .post()
                .uri(authServiceUrl + "/api/users/" + userId + "/picture")
                .header("Authorization", ensureBearer(token))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(builder.build())
                .retrieve()
                .bodyToMono(Void.class);
    }

    @Override
    public Mono<Void> deleteRemoteAccount(UUID userId, String token) {
        return webClientBuilder.build()
                .delete()
                .uri(authServiceUrl + "/api/users/" + userId)
                .header("Authorization", ensureBearer(token))
                .retrieve()
                .bodyToMono(Void.class);
    }

    @Override
    public Mono<AuthResponse> refresh(String refreshToken) {
        return authApiClient.refreshToken(new AuthApiClient.RefreshRequest(refreshToken))
                .map(this::mapToAuthResponse);
    }

    @Override
    public Mono<Boolean> roleExists(String roleName) {
        return Mono.just(true); 
    }

    @Override
    public Mono<Void> createRole(String roleName) {
        return Mono.empty();
    }

    // Mappers
    private String ensureBearer(String token) {
        if (token == null) return "";
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }

    private AuthResponse mapToAuthResponse(AuthApiClient.TraMaSysResponse res) {
        return new AuthResponse(res.accessToken(), res.refreshToken(), mapToDomainUserDetail(res.user()));
    }

    private UserDetail mapToDomainUserDetail(AuthApiClient.UserDetailResponse res) {
        if (res == null) return null;
        return new UserDetail(
            res.id(), res.username(), res.email(), res.phone(),
            res.firstName(), res.lastName(), res.service(),
            res.roles(), res.permissions(), 
            null, 
            null, null, null
        );
    }
}