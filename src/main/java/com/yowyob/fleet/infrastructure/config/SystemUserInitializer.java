package com.yowyob.fleet.infrastructure.config;

import com.yowyob.fleet.infrastructure.adapters.outbound.external.client.GeofenceAuthClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemUserInitializer {

    private final GeofenceAuthClient geofenceAuthClient;

    @Value("${application.geofence-system-user.username}")
    private String username;

    @Value("${application.geofence-system-user.password}")
    private String password;

    @EventListener(ApplicationReadyEvent.class)
    public void initSystemUserInGeofence() {
        log.info("🚀 Synchronisation de l'utilisateur système dans le moteur Geofence...");

        Map<String, Object> regRequest = new HashMap<>();
        regRequest.put("firstname", "System");
        regRequest.put("lastname", "Fleet");
        regRequest.put("username", username);
        regRequest.put("phoneNumber", "+237000000000");
        regRequest.put("email", "system@yowyob.com");
        regRequest.put("password", password);
        regRequest.put("password_confirmation", password);
        regRequest.put("DOB", "1990-01-01"); // Date de naissance requise

        geofenceAuthClient.register(regRequest)
                .doOnSuccess(res -> log.info("✅ Utilisateur système créé dans la DB Geofence."))
                .onErrorResume(e -> {
                    log.info("ℹ️ L'utilisateur système existe déjà dans le Geofence ou le service est indisponible.");
                    return Mono.empty();
                })
                .subscribe();
    }
}