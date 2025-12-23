package com.yowyob.fleet.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.core.DatabaseClient;
import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Configuration
@Profile("local")
public class DatabaseInitConfig {

    private final DatabaseClient databaseClient;

    public DatabaseInitConfig(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @PostConstruct
    public void init() {
        System.out.println("⚙️ LOCAL ENVIRONMENT: Starting database initialization...");

        // 1. Run Schema -> 2. Seed if empty
        executeSqlFile("local/schema.sql")
                .then(checkIfDataExists())
                .flatMap(hasData -> {
                    if (!hasData) {
                        System.out.println("🌱 Seeding: Database is empty. Injecting test data...");
                        return executeSqlFile("local/data.sql");
                    }
                    System.out.println("✅ Seeding: Data already exists. Skipping.");
                    return Mono.empty();
                })
                .subscribe(
                        null,
                        err -> {
                            System.err.println("❌ DB INIT ERROR: " + err.getMessage());
                            // Log the error but don't stop the app
                        },
                        () -> System.out.println("🚀 LOCAL DATABASE READY!")
                );
    }

    private Mono<Void> executeSqlFile(String filePath) {
        return Mono.fromCallable(() -> {
            ClassPathResource resource = new ClassPathResource(filePath);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        })
        .flatMapMany(sql -> {
            // Split by semicolon
            return Flux.fromArray(sql.split(";"));
        })
        .map(String::trim)
        .filter(s -> !s.isEmpty()) // We only filter out truly empty strings
        .concatMap(s -> {
            // Execute each statement sequentially
            return databaseClient.sql(s)
                    .then()
                    .onErrorResume(e -> {
                        // Ignore errors on DROP commands (often happen if object doesn't exist)
                        if (s.toUpperCase().startsWith("DROP")) {
                            return Mono.empty();
                        }
                        return Mono.error(e);
                    });
        })
        .then();
    }

    private Mono<Boolean> checkIfDataExists() {
        return databaseClient.sql("SELECT COUNT(*) FROM users")
                .map((row, metadata) -> row.get(0, Long.class))
                .first()
                .map(count -> count > 0)
                .onErrorReturn(false);
    }
}