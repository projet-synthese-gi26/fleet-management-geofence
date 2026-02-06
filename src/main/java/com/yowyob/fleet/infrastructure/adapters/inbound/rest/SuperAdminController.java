package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import com.yowyob.fleet.domain.ports.in.AuthUseCase;
import com.yowyob.fleet.domain.ports.in.ManageSuperAdminUseCase;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/super")
@RequiredArgsConstructor
@Tag(name = "04. Super Admin | Gestion des Administrateurs")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('FLEET_SUPER_ADMIN')")
public class SuperAdminController {

    private final ManageSuperAdminUseCase superAdminUseCase;

    public record CreateAdminRequest(
        @NotBlank String username, @NotBlank String password,
        @Email @NotBlank String email, @NotBlank String phone,
        @NotBlank String firstName, @NotBlank String lastName
    ) {}

    @PostMapping("/admins")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un nouvel Administrateur")
    public Mono<AuthPort.AuthResponse> create(@Valid @RequestBody CreateAdminRequest req) {
        return superAdminUseCase.createAdmin(new AuthUseCase.RegisterCommand(
            req.username(), req.password(), req.email(), req.phone(), req.firstName(), req.lastName(), null, null
        ));
    }

    @GetMapping("/admins")
    @Operation(summary = "Lister les Administrateurs")
    public Flux<AuthPort.UserDetail> list(@Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String t) {
        return superAdminUseCase.listAdmins(t);
    }

    @GetMapping("/admins/{id}")
    @Operation(summary = "Détails d'un Administrateur")
    public Mono<AuthPort.UserDetail> getOne(@PathVariable UUID id, @Parameter(hidden = true) @RequestHeader(HttpHeaders.AUTHORIZATION) String t) {
        return superAdminUseCase.getAdminDetails(id, t);
    }

    @PatchMapping("/admins/{id}/toggle")
    @Operation(summary = "Activer/Désactiver un Administrateur")
    public Mono<Void> toggle(@PathVariable UUID id, Authentication auth) {
        AuthPort.UserDetail currentUser = (AuthPort.UserDetail) auth.getPrincipal();
        return superAdminUseCase.toggleAdminStatus(id, currentUser.id());
    }
}