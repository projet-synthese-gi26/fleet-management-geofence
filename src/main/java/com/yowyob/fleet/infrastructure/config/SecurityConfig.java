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
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity // Permet d'utiliser @PreAuthorize dans les controllers
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationManager authenticationManager;
    private final BearerTokenServerAuthenticationConverter authenticationConverter;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        
        // Configuration du filtre manuel JWT
        AuthenticationWebFilter jwtFilter = new AuthenticationWebFilter(authenticationManager);
        jwtFilter.setServerAuthenticationConverter(authenticationConverter);

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
                        // 1. Swagger & Monitoring (Public)
                        .pathMatchers(
                            "/v3/api-docs/**", 
                            "/swagger-ui/**", 
                            "/swagger-ui.html", 
                            "/webjars/**",
                            "/actuator/**",
                            "/api/v1/health/**"
                        ).permitAll()
                        
                        // 2. Auth Endpoints (Public)
                        .pathMatchers("/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/refresh").permitAll()
                        
                        // 3. Tout le reste nécessite un Token
                        .anyExchange().authenticated()
                )
                
                // Ajout du filtre JWT dans la chaîne
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                
                .build();
    }
}