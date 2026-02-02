package com.yowyob.fleet.infrastructure.config.bootstrap;

import com.yowyob.fleet.application.service.VehicleTypeService;
import com.yowyob.fleet.domain.ports.in.AuthUseCase;
import com.yowyob.fleet.domain.ports.out.AuthPort;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.VehicleTypeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
// On active ce loader sur tous les profils, car c'est des données vitales
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

        // On chaine les opérations de manière séquentielle pour le démarrage
        seedVehicleTypes()
                .then(seedSuperAdmin())
                .block(Duration.ofMinutes(1)); // Timeout de sécurité

        log.info("✅ Initialisation des données terminée.");
    }

    private Mono<Void> seedVehicleTypes() {
        return vehicleTypeService.getAllTypes()
                .hasElements()
                .flatMap(hasElements -> {
                    if (hasElements) {
                        log.info("ℹ️ Types de véhicules déjà présents. Pas de seeding nécessaire.");
                        return Mono.empty();
                    }

                    log.info("🌱 Seeding des types de véhicules standards...");
                    return Flux.just(
                            new VehicleTypeRequest("CAR", "Voiture", "Véhicule léger de tourisme (max 5 places)"),
                            new VehicleTypeRequest("VAN", "Fourgonnette", "Véhicule utilitaire léger ou transport de groupe"),
                            new VehicleTypeRequest("TRUCK", "Camion", "Poids lourd pour le transport de marchandises"),
                            new VehicleTypeRequest("BIKE", "Moto", "Deux roues motorisé pour livraison rapide"),
                            new VehicleTypeRequest("BUS", "Autobus", "Transport en commun grande capacité")
                    )
                    .flatMap(vehicleTypeService::createType)
                    .then();
                });
    }

    private Mono<Void> seedSuperAdmin() {
        // 1. Tenter de se connecter pour voir si l'admin existe
        return authPort.login(adminEmail, adminPassword)
                .flatMap(response -> {
                    log.info("ℹ️ Super Admin déjà existant (ID: {}).", response.user().id());
                    return Mono.empty();
                })
                .onErrorResume(e -> {
                    // 2. Si erreur (Login échoué), on tente de le créer
                    log.info("⚠️ Super Admin non détecté. Tentative de création...");
                    
                    AuthUseCase.RegisterCommand command = new AuthUseCase.RegisterCommand(
                            adminUsername,
                            adminPassword,
                            adminEmail,
                            adminPhone,
                            adminFirstName,
                            adminLastName,
                            List.of("FLEET_ADMIN", "FLEET_SUPER_ADMIN"), // On donne les droits max
                            null // Pas de photo pour l'instant
                    );

                    return authPort.registerInRemote(command)
                            .doOnSuccess(resp -> log.info("✅ Super Admin créé avec succès ! (ID: {})", resp.user().id()))
                            .doOnError(err -> log.error("❌ Erreur lors de la création du Super Admin : {}", err.getMessage()))
                            .then();
                })
                .then();
    }
}