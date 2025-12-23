package com.yowyob.fleet.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /**
     * Configures the security filter chain for WebFlux.
     */
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        System.out.println("🛡️ REACTIVE SECURITY CONFIGURATION LOADED");

        return http
                // Disable CSRF as we are building a stateless REST API
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                
                .authorizeExchange(exchanges -> exchanges
                        // Allow access to health checks and monitoring
                        .pathMatchers("/actuator/**").permitAll()
                        
                        // Allow access to Swagger UI and API Documentation (v3)
                        .pathMatchers(
                                "/v3/api-docs/**", 
                                "/swagger-ui/**", 
                                "/swagger-ui.html", 
                                "/webjars/**",
                                "/api/v1/health/**"
                        ).permitAll()
                        
                        // All other exchanges require authentication
                        .anyExchange().authenticated()
                )
                
                // Disable default login forms and basic auth
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .build();
    }

    /**
     * Provides a dummy technical user to satisfy Spring Security's requirements
     * and disable the auto-generated password in the logs.
     */
    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        UserDetails dummyUser = User.withUsername("technical_user")
                .password("{noop}dummy_password") // {noop} means no encoding
                .roles("SYSTEM")
                .build();
        return new MapReactiveUserDetailsService(dummyUser);
    }
}