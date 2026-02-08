package com.yowyob.fleet.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class OpenApiConfig {

    // --- 1. DÉFINITION DES CONSTANTES (SPOF: Single Point Of Failure) ---
    public static final String TAG_MONITORING = "01. Monitoring";
    public static final String TAG_AUTH = "02. Auth";
    public static final String TAG_ACCOUNT = "03. Account";
    public static final String TAG_SUPER_ADMIN = "04. Super Admin | Gestion des Administrateurs";
    public static final String TAG_ADMIN_MANAGERS = "05. Admin | Gestion des Fleet Managers";
    public static final String TAG_ADMIN_RESOURCES = "06. Admin | Gestion des Ressources";
    public static final String TAG_FLEET_MANAGERS = "07. Fleet Managers";
    public static final String TAG_DRIVERS = "08. Drivers";
    public static final String TAG_VEHICLES = "09. Vehicles";
    public static final String TAG_FLEETS = "10. Fleets";
    public static final String TAG_TRIPS = "11. Trips";
    public static final String TAG_GEOFENCING = "12. Geofencing";
    public static final String TAG_PAYMENTS = "13. Payments";

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("YowYob Fleet Management API")
                        .version("1.0.0")
                        .description("API Réactive pour la gestion de flottes et le géorepérage.")
                        .contact(new Contact().name("Gabriel Nomo").email("g.nomo@yowyob.com")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

    @Bean
    public OpenApiCustomizer sortTagsAlphabetically() {
        return openApi -> {
            // --- 2. DÉFINITION DE L'ORDRE STRICT ---
            List<String> order = List.of(
                    TAG_MONITORING,
                    TAG_AUTH,
                    TAG_ACCOUNT,
                    TAG_SUPER_ADMIN,
                    TAG_ADMIN_MANAGERS,
                    TAG_ADMIN_RESOURCES,
                    TAG_FLEET_MANAGERS,
                    TAG_DRIVERS,
                    TAG_VEHICLES,
                    TAG_FLEETS,
                    TAG_TRIPS,
                    TAG_GEOFENCING,
                    TAG_PAYMENTS
            );

            if (openApi.getTags() != null) {
                openApi.setTags(openApi.getTags().stream()
                        .sorted(Comparator.comparingInt(tag -> {
                            int index = order.indexOf(tag.getName());
                            // Si le tag n'est pas dans la liste, on le met à la fin (999)
                            return index == -1 ? 999 : index;
                        }))
                        .collect(Collectors.toList()));
            }
        };
    }
}