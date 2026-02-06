package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import com.yowyob.fleet.domain.ports.in.AuthUseCase;
import com.yowyob.fleet.domain.ports.out.AuthPort;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.LoginRequest;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "02. Auth", description = "Endpoints publics (Login, Register, Refresh)")
public class AuthController {

    private final AuthUseCase authUseCase;

    @PostMapping("/login")
    @Operation(summary = "Connexion utilisateur", description = "Retourne les tokens et déclenche la synchronisation du profil local.")
    public Mono<AuthPort.AuthResponse> login(@RequestBody LoginRequest request) {
        return authUseCase.login(request.identifier(), request.password());
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rafraîchir le token", description = "Utilisez le refreshToken pour obtenir un nouvel accessToken.")
    public Mono<AuthPort.AuthResponse> refresh(@RequestBody TokenRefreshRequest request) {
        return authUseCase.refreshToken(request.refreshToken());
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Inscription Utilisateur",
        description = "Création de compte. Collez le JSON dans 'user' et l'image dans 'file'.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                encoding = @Encoding(name = "user", contentType = "application/json")
            )
        )
    )
    public Mono<AuthPort.AuthResponse> register(
            @RequestPart("user") RegisterRequest dto,
            @RequestPart(value = "file", required = false) FilePart filePart 
    ) {
        Mono<AuthUseCase.FileContent> photoMono = Mono.justOrEmpty(filePart)
                .flatMap(fp -> DataBufferUtils.join(fp.content())
                        .map(dataBuffer -> {
                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
                            dataBuffer.read(bytes);
                            DataBufferUtils.release(dataBuffer);
                            return new AuthUseCase.FileContent(
                                    fp.filename(),
                                    fp.headers().getContentType() != null ? fp.headers().getContentType().toString() : "image/jpeg",
                                    bytes
                            );
                        }));

        return photoMono
                .map(photo -> new AuthUseCase.RegisterCommand(
                        dto.username(), dto.password(), dto.email(), dto.phone(),
                        dto.firstName(), dto.lastName(), dto.roles(), photo
                ))
                .switchIfEmpty(Mono.just(new AuthUseCase.RegisterCommand(
                        dto.username(), dto.password(), dto.email(), dto.phone(),
                        dto.firstName(), dto.lastName(), dto.roles(), null
                )))
                .flatMap(authUseCase::register);
    }

    public record TokenRefreshRequest(String refreshToken) {}
}