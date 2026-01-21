package com.yowyob.fleet.infrastructure.config;

import com.yowyob.fleet.infrastructure.config.security.BearerTokenServerAuthenticationConverter;
import com.yowyob.fleet.infrastructure.config.security.JwtAuthenticationManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationManager authenticationManager;
    private final BearerTokenServerAuthenticationConverter authenticationConverter;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        
        // --- 1. CONFIGURATION DU FILTRE JWT ---
        AuthenticationWebFilter jwtFilter = new AuthenticationWebFilter(authenticationManager);
        jwtFilter.setServerAuthenticationConverter(authenticationConverter);

        // CORRECTION ROBUSTE : Le filtre ne s'active QUE sur les routes protégées.
        // Il ignorera totalement les headers sur /auth/*, évitant le crash 500.
        jwtFilter.setRequiresAuthenticationMatcher(
            ServerWebExchangeMatchers.pathMatchers(
                "/api/v1/account/**",
                "/api/v1/fleets/**",
                "/api/v1/drivers/**",
                "/api/v1/vehicles/**",
                "/api/v1/geofence/**",
                "/api/v1/admin/**"
            )
        );

        // --- 2. CHAÎNE DE SÉCURITÉ ---
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                
                // Gestion explicite des erreurs 401/403
                .exceptionHandling(handling -> handling
                    .authenticationEntryPoint((exchange, e) -> 
                        Mono.fromRunnable(() -> exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED)))
                    .accessDeniedHandler((exchange, e) -> 
                        Mono.fromRunnable(() -> exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN)))
                )

                .authorizeExchange(exchanges -> exchanges
                        // Routes Publiques (Redondant mais sécurité double couche)
                        .pathMatchers(
                            "/v3/api-docs/**", 
                            "/swagger-ui/**", 
                            "/swagger-ui.html", 
                            "/webjars/**",
                            "/actuator/**",
                            "/api/v1/health/**",
                            "/api/v1/auth/**" // Login, Register, Refresh
                        ).permitAll()
                        
                        // Tout le reste nécessite un Token
                        .anyExchange().authenticated()
                )
                
                // Ajout du filtre JWT
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                
                .build();
    }
}