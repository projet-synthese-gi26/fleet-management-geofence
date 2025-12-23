package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class HealthCheckController {

    private final DatabaseClient databaseClient;

    /**
     * Checks the number of users in the local database to validate seeding.
     */
    @GetMapping("/users-count")
    public Mono<Map<String, Object>> getUserCount() {
        return databaseClient.sql("SELECT COUNT(*) FROM users")
                .map((row, metadata) -> row.get(0, Long.class))
                .first()
                .map(count -> Map.of(
                        "status", "UP",
                        "database", "CONNECTED",
                        "users_in_db", count
                ));
    }
}