package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import com.yowyob.fleet.domain.ports.in.AuthUseCase;
import com.yowyob.fleet.domain.ports.out.AuthPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "00. Super Admin", description = "Gestion des Administrateurs Fleet")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('FLEET_SUPER_ADMIN')")
public class AdminUserController {

    private final AuthPort authPort;

    @PostMapping("/create-admin")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Nommer un Administrateur", description = "Crée un compte avec le rôle FLEET_ADMIN.")
    public Mono<AuthPort.AuthResponse> createAdmin(@Valid @RequestBody CreateAdminRequest request) {
        AuthUseCase.RegisterCommand command = new AuthUseCase.RegisterCommand(
            request.username, request.password, request.email, request.phone,
            request.firstName, request.lastName, 
            List.of("FLEET_ADMIN"), // Rôle forcé
            null
        );
        return authPort.registerInRemote(command);
    }

    @GetMapping
    @Operation(summary = "Lister tous les utilisateurs de l'écosystème Fleet")
    public Flux<AuthPort.UserDetail> getAllUsers(
        @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String token
    ) {
        return authPort.getAllUsers(token);
    }

    @GetMapping("/{id}")
    public Mono<AuthPort.UserDetail> getUser(
        @PathVariable UUID id, 
        @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String token
    ) {
        return authPort.getUserById(id, token);
    }

    // DTO Interne
    public record CreateAdminRequest(
        @NotBlank String username,
        @NotBlank String password,
        @Email @NotBlank String email,
        @NotBlank String phone,
        @NotBlank String firstName,
        @NotBlank String lastName
    ) {}
}