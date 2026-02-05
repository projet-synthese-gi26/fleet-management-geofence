package com.yowyob.fleet.infrastructure.config.bootstrap;

import com.yowyob.fleet.application.service.VehicleTypeService;
import com.yowyob.fleet.domain.ports.in.AuthUseCase;
import com.yowyob.fleet.domain.ports.out.AuthPort;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.VehicleTypeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitialDataLoader implements CommandLineRunner {

    private final VehicleTypeService vehicleTypeService;
    private final AuthPort authPort;

    @Value("${application.bootstrap.admin.email}")
    private String adminEmail;

    @Value("${application.bootstrap.admin.password}")
    private String adminPassword;

    @Value("${application.bootstrap.admin.username}")
    private String adminUsername;

    @Value("${application.bootstrap.admin.phone}")
    private String adminPhone;

    @Value("${application.bootstrap.admin.firstname}")
    private String adminFirstName;

    @Value("${application.bootstrap.admin.lastname}")
    private String adminLastName;

    @Override
    public void run(String... args) {
        log.info("🚀 Démarrage du Seeder de données initiales...");

        seedVehicleTypes()
                .then(seedSuperAdmin())
                .timeout(Duration.ofMinutes(1))
                // FIX CRITIQUE : Empêcher le crash de l'app si le seeding échoue
                .onErrorResume(e -> {
                    log.error("⚠️ Le seeding a échoué mais l'application va démarrer : {}", e.getMessage());
                    return Mono.empty();
                })
                .block();

        log.info("✅ Phase d'initialisation terminée.");
    }

    private Mono<Void> seedVehicleTypes() {
        return vehicleTypeService.getAllTypes()
                .hasElements()
                .flatMap(hasElements -> {
                    if (hasElements) return Mono.empty();
                    log.info("🌱 Seeding des types de véhicules standards...");
                    return Flux.just(
                            new VehicleTypeRequest("CAR", "Voiture", "Véhicule léger"),
                            new VehicleTypeRequest("VAN", "Fourgonnette", "Transport de groupe"),
                            new VehicleTypeRequest("TRUCK", "Camion", "Poids lourd"),
                            new VehicleTypeRequest("BIKE", "Moto", "Deux roues"),
                            new VehicleTypeRequest("BUS", "Autobus", "Transport commun")
                    )
                    .flatMap(vehicleTypeService::createType)
                    .then();
                })
                .onErrorResume(e -> {
                    log.warn("Impossible de seeder les types (probablement déjà présents) : {}", e.getMessage());
                    return Mono.empty();
                });
    }

    private Mono<Void> seedSuperAdmin() {
        // Tenter de se connecter
        return authPort.login(adminEmail, adminPassword)
                .doOnSuccess(resp -> log.info("ℹ️ Super Admin déjà opérationnel."))
                .onErrorResume(e -> {
                    log.info("⚠️ Login Admin échoué ({}). Tentative de création...", e.getMessage());
                    
                    AuthUseCase.RegisterCommand command = new AuthUseCase.RegisterCommand(
                            adminUsername, adminPassword, adminEmail, adminPhone,
                            adminFirstName, adminLastName, List.of("FLEET_ADMIN", "FLEET_SUPER_ADMIN"), null
                    );

                    return authPort.registerInRemote(command)
                            .doOnSuccess(resp -> log.info("✅ Super Admin créé avec succès !"))
                            .onErrorResume(err -> {
                                // FIX : Si 409 Conflict, c'est que l'user existe déjà sur le service Auth
                                // mais avec un mot de passe différent de notre application.yml.
                                // On considère cela comme un "Succès" (l'user est là).
                                if (err instanceof WebClientResponseException && 
                                   ((WebClientResponseException) err).getStatusCode() == HttpStatus.CONFLICT) {
                                    log.info("ℹ️ L'utilisateur Super Admin existe déjà sur le service Auth.");
                                    return Mono.empty();
                                }
                                return Mono.error(err);
                            });
                })
                .then();
    }
}