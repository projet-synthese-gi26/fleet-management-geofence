package com.yowyob.fleet.application.service;

import com.yowyob.fleet.domain.ports.in.AuthUseCase;
import com.yowyob.fleet.domain.ports.out.AuthPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

    private final AuthPort authPort;

    @Override
    public Mono<AuthPort.AuthResponse> login(String identifier, String password) {
        return authPort.login(identifier, password);
    }

    @Override
    public Mono<AuthPort.AuthResponse> register(String username, String email, String password, String phone, String firstName, String lastName) {
        return authPort.register(username, password, email, phone, firstName, lastName);
    }

    @Override
    public Mono<Void> resetPassword(String email) {
        return authPort.forgotPassword(email);
    }
}