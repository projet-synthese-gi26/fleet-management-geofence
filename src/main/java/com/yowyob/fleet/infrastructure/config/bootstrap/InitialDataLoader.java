package com.yowyob.fleet.infrastructure.config.bootstrap;

import com.yowyob.fleet.application.service.VehicleTypeService;
import com.yowyob.fleet.domain.ports.in.AuthUseCase;
import com.yowyob.fleet.domain.ports.out.AuthPort;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.VehicleTypeRequest;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.FuelTypeEntity;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.ManufacturerEntity;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.FuelTypeR2dbcRepository;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.ManufacturerR2dbcRepository;

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
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitialDataLoader implements CommandLineRunner {

    private final VehicleTypeService vehicleTypeService;
    private final AuthPort authPort;
    private final ManufacturerR2dbcRepository mfrRepo;
    private final FuelTypeR2dbcRepository fuelRepo;

    @Value("${application.bootstrap.admin.email}") private String adminEmail;
    @Value("${application.bootstrap.admin.password}") private String adminPassword;
    @Value("${application.bootstrap.admin.username}") private String adminUsername;
    @Value("${application.bootstrap.admin.phone}") private String adminPhone;
    @Value("${application.bootstrap.admin.firstname}") private String adminFirstName;
    @Value("${application.bootstrap.admin.lastname}") private String adminLastName;

    @Override
    public void run(String... args) {
        log.info("🚀 Démarrage du Seeder de données initiales...");

        // Exécution séquentielle et sécurisée
        seedVehicleTypes()
                .then(seedManufacturers())
                .then(seedFuelTypes())
                .then(seedSuperAdmin())
                .timeout(Duration.ofMinutes(2)) // Un peu plus de temps pour le réseau
                .onErrorResume(e -> {
                    log.error("⚠️ Le seeding a été interrompu (Erreur non critique) : {}", e.getMessage());
                    return Mono.empty();
                })
                .block();

        log.info("✅ Phase d'initialisation terminée.");
    }

    private Mono<Void> seedVehicleTypes() {
        return vehicleTypeService.getAllTypes()
                .hasElements()
                .flatMap(exists -> {
                    if (exists) return Mono.empty();
                    log.info("🌱 Seeding : Vehicle Types...");
                    return Flux.just(
                            new VehicleTypeRequest("CAR", "Voiture", "Véhicule léger"),
                            new VehicleTypeRequest("VAN", "Fourgonnette", "Transport de groupe"),
                            new VehicleTypeRequest("TRUCK", "Camion", "Poids lourd"),
                            new VehicleTypeRequest("BIKE", "Moto", "Deux roues"),
                            new VehicleTypeRequest("BUS", "Autobus", "Transport commun")
                    ).flatMap(vehicleTypeService::createType).then();
                });
    }

    private Mono<Void> seedManufacturers() {
        return mfrRepo.count().flatMap(count -> {
            if (count > 0) return Mono.empty();
            log.info("🌱 Seeding : Manufacturers...");
            return Flux.just(
                new ManufacturerEntity(UUID.randomUUID(), "TOYOTA", "Toyota", "Japon", true),
                new ManufacturerEntity(UUID.randomUUID(), "HYUNDAI", "Hyundai", "Corée", true),
                new ManufacturerEntity(UUID.randomUUID(), "MERCEDES", "Mercedes-Benz", "Allemagne", true),
                new ManufacturerEntity(UUID.randomUUID(), "RENAULT", "Renault", "France", true),
                new ManufacturerEntity(UUID.randomUUID(), "SUZUKI", "Suzuki", "Japon", true)
            ).flatMap(mfrRepo::save).then();
        });
    }

    private Mono<Void> seedFuelTypes() {
        return fuelRepo.count().flatMap(count -> {
            if (count > 0) return Mono.empty();
            log.info("🌱 Seeding : Fuel Types...");
            return Flux.just(
                new FuelTypeEntity(UUID.randomUUID(), "ESSENCE", "Essence", "Super", true),
                new FuelTypeEntity(UUID.randomUUID(), "DIESEL", "Gasoil", "Diesel", true),
                new FuelTypeEntity(UUID.randomUUID(), "ELECTRIC", "Électrique", "EV", true),
                new FuelTypeEntity(UUID.randomUUID(), "HYBRID", "Hybride", "HEV", true)
            ).flatMap(fuelRepo::save).then();
        });
    }

    private Mono<Void> seedSuperAdmin() {
        // 1. On vérifie d'abord si on peut se loguer (déjà existant)
        return authPort.login(adminEmail, adminPassword)
                .doOnSuccess(resp -> log.info("ℹ️ Super Admin déjà opérationnel."))
                .onErrorResume(e -> {
                    // 2. Sinon on tente la création
                    log.info("⚠️ Login Admin échoué. Tentative de création du Super Admin...");
                    AuthUseCase.RegisterCommand command = new AuthUseCase.RegisterCommand(
                            adminUsername, adminPassword, adminEmail, adminPhone,
                            adminFirstName, adminLastName, List.of("FLEET_ADMIN", "FLEET_SUPER_ADMIN"), null
                    );
                    return authPort.registerInRemote(command)
                            .doOnSuccess(r -> log.info("✅ Super Admin créé."))
                            .onErrorResume(err -> {
                                // Cas où l'user existe chez Pynfi mais le mot de passe du .yml est différent
                                if (err instanceof WebClientResponseException w && w.getStatusCode() == HttpStatus.CONFLICT) {
                                    log.info("ℹ️ Super Admin existe déjà sur le service distant.");
                                    return Mono.empty();
                                }
                                return Mono.error(err);
                            });
                }).then();
    }
}