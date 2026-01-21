package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Tag(name = "01. Monitoring", description = "Diagnostic complet de l'infrastructure")
public class HealthCheckController {

    private final DatabaseClient databaseClient;
    private final ReactiveRedisConnectionFactory redisConnectionFactory;
    
    // On injecte le template pour vérifier si Kafka est configuré
    // Note: Pour un vrai check de santé Kafka, il faudrait AdminClient, mais c'est complexe en réactif pur sans dépendance lourde.
    // Ici on vérifie juste que le bean est up et on tente une opération non intrusive si possible.
    private final ReactiveKafkaProducerTemplate<String, Object> kafkaTemplate;

    @Value("${application.auth.url}")
    private String authUrl;

    @Value("${application.external.vehicle-service-url}")
    private String vehicleUrl;

    private final WebClient.Builder webClientBuilder;

    @GetMapping("/diagnostic")
    @Operation(summary = "Diagnostic Système complet", description = "Vérifie DB, Redis, Kafka et les APIs distantes.")
    public Mono<SystemHealth> getDiagnostic() {
        // Lancement parallèle des checks
        return Mono.zip(
            checkDatabase(),
            checkRedis(),
            checkKafka(),
            checkRemoteService("Auth Service", authUrl),
            checkRemoteService("Vehicle Service", vehicleUrl)
        ).map(tuple -> {
            Map<String, ServiceStatus> dependencies = new HashMap<>();
            dependencies.put("database", tuple.getT1());
            dependencies.put("redis", tuple.getT2());
            dependencies.put("kafka", tuple.getT3());
            dependencies.put("auth_service", tuple.getT4());
            dependencies.put("vehicle_service", tuple.getT5());

            // Si l'un des services critiques (DB, Redis) est DOWN, le global est DOWN
            boolean isGlobalUp = tuple.getT1().status().equals("UP") 
                              && tuple.getT2().status().equals("UP");

            return new SystemHealth(
                isGlobalUp ? "UP" : "DEGRADED",
                Instant.now(),
                dependencies
            );
        });
    }

    // --- CHECKS INDIVIDUELS ---

    private Mono<ServiceStatus> checkDatabase() {
        return databaseClient.sql("SELECT 1")
                .fetch()
                .first()
                .map(r -> new ServiceStatus("UP", "PostgreSQL R2DBC Connected"))
                .timeout(Duration.ofSeconds(2))
                .onErrorResume(e -> Mono.just(new ServiceStatus("DOWN", e.getMessage())));
    }

    private Mono<ServiceStatus> checkRedis() {
        return redisConnectionFactory.getReactiveConnection()
                .ping()
                .map(resp -> new ServiceStatus("UP", "Redis PONG received"))
                .timeout(Duration.ofSeconds(2))
                .onErrorResume(e -> Mono.just(new ServiceStatus("DOWN", e.getMessage())));
    }

    private Mono<ServiceStatus> checkKafka() {
        // Check simple : est-ce que le contexte Kafka est chargé ?
        // Pour aller plus loin, il faudrait envoyer un message test, ce qui est intrusif.
        if (kafkaTemplate != null) {
            return Mono.just(new ServiceStatus("UP", "Kafka Producer Configured"));
        }
        return Mono.just(new ServiceStatus("UNKNOWN", "Kafka template not available"));
    }

    private Mono<ServiceStatus> checkRemoteService(String name, String url) {
        if (url == null || url.isBlank()) {
            return Mono.just(new ServiceStatus("DISABLED", "No URL configured"));
        }
        
        // On suppose que les services ont un endpoint /actuator/health ou répondent sur la racine
        // Pour être générique, on ping la racine ou une route connue.
        // Ici on tente simplement d'atteindre l'URL.
        return webClientBuilder.build()
                .get()
                .uri(url) // Attention: si l'URL est juste le domaine, il faut peut-être ajouter un path
                .retrieve()
                .toBodilessEntity()
                .map(response -> new ServiceStatus("UP", "HTTP " + response.getStatusCode()))
                .timeout(Duration.ofSeconds(3))
                .onErrorResume(e -> {
                    String msg = e.getMessage();
                    if (msg != null && msg.contains("Connection refused")) {
                        msg = "Unreachable";
                    }
                    return Mono.just(new ServiceStatus("DOWN", msg));
                });
    }

    // --- DTOs ---
    public record SystemHealth(String status, Instant timestamp, Map<String, ServiceStatus> dependencies) {}
    public record ServiceStatus(String status, String details) {}
}