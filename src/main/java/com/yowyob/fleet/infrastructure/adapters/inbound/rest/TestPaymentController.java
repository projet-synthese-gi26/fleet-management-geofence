package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import com.yowyob.fleet.domain.ports.out.ExternalPaymentPort;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/test/payments")
@RequiredArgsConstructor
public class TestPaymentController {

    private final ExternalPaymentPort paymentPort;

    @GetMapping("/balance/{ownerId}")
    public Mono<BigDecimal> getBalance(@PathVariable UUID ownerId) {
        return paymentPort.getBalance(ownerId);
    }

    @PostMapping("/wallet")
    public Mono<UUID> createWallet(@RequestParam UUID ownerId, @RequestParam String name) {
        return paymentPort.initializeWallet(ownerId, name);
    }
}