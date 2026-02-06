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
            List<String> order = List.of(
                "01. Monitoring",
                "02. Auth",
                "03. Account",
                "04. Super Admin",
                "05. Admin",
                "06. Fleet Managers",
                "07. Drivers",
                "08. Vehicles",
                "09. Fleets",
                "10. Trips",
                "11. Geofencing",
                "12. Payments"
            );

            if (openApi.getTags() != null) {
                openApi.setTags(openApi.getTags().stream()
                        .sorted(Comparator.comparingInt(tag -> {
                            int index = order.indexOf(tag.getName());
                            return index == -1 ? 999 : index;
                        }))
                        .collect(Collectors.toList()));
            }
        };
    }
}