package com.yowyob.fleet.infrastructure.config.security;

import com.yowyob.fleet.domain.ports.out.AuthPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final AuthPort authPort;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = (String) authentication.getCredentials();

        // On appelle le service distant pour valider le token
        return authPort.getUserProfile(token)
                .map(userDetail -> {
                    log.debug("Token validé pour user: {}", userDetail.username());
                    
                    // Conversion des rôles
                    var authorities = userDetail.roles().stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList());

                    // FIX : On type explicitement en 'Authentication' pour satisfaire le compilateur
                    Authentication auth = new UsernamePasswordAuthenticationToken(
                            userDetail, 
                            token, 
                            authorities
                    );
                    
                    return auth;
                })
                .onErrorResume(e -> {
                    log.warn("Token invalide ou service auth indisponible: {}", e.getMessage());
                    return Mono.empty();
                });
    }
}