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
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers; // On peut retirer cet import si non utilisé ailleurs
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
        
        // MODIFICATION : On retire le 'setRequiresAuthenticationMatcher'. 
        // Par défaut, le filtre s'appliquera désormais à TOUTES les requêtes.
        // Si pas de token -> Le convertisseur renvoie vide -> Le filtre laisse passer en "Anonyme".

        // --- 2. CHAÎNE DE SÉCURITÉ ---
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                
                .authenticationManager(authenticationManager) 
                
                .exceptionHandling(handling -> handling
                    .authenticationEntryPoint((exchange, e) -> 
                        Mono.fromRunnable(() -> exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED)))
                    .accessDeniedHandler((exchange, e) -> 
                        Mono.fromRunnable(() -> exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN)))
                )

                .authorizeExchange(exchanges -> exchanges
                        // A. ROUTES PUBLIQUES (Pas besoin de token, même si le filtre a essayé d'en trouver un)
                        .pathMatchers(
                            "/v3/api-docs/**", 
                            "/swagger-ui/**", 
                            "/swagger-ui.html", 
                            "/webjars/**",
                            "/actuator/**",
                            "/api/v1/health/**",
                            "/api/v1/auth/**"
                        ).permitAll()
                        
                        // B. TOUT LE RESTE = AUTHENTIFICATION OBLIGATOIRE
                        // Si le filtre n'a pas trouvé de token valide, ça bloquera ici (401).
                        .anyExchange().authenticated()
                )
                
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                
                .build();
    }
}