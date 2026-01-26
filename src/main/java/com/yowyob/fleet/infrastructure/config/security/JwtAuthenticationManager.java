package com.yowyob.fleet.infrastructure.config.security;

import com.yowyob.fleet.domain.ports.out.AuthPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException; // Import ajouté
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
                    
                    var authorities = userDetail.roles().stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList());

                    Authentication auth = new UsernamePasswordAuthenticationToken(
                            userDetail, 
                            token, 
                            authorities
                    );
                    
                    return auth;
                })
                // CORRECTION : On ne retourne plus Mono.empty() mais une erreur explicite
                .onErrorResume(e -> {
                    log.warn("Authentification échouée : {}", e.getMessage());
                    return Mono.error(new BadCredentialsException("Token invalide ou expiré"));
                });
    }
}