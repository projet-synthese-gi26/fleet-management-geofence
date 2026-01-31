package com.yowyob.fleet.infrastructure.adapters.outbound.external.client;

import com.yowyob.fleet.infrastructure.adapters.outbound.external.dto.WalletCreationRequest;
import com.yowyob.fleet.infrastructure.adapters.outbound.external.dto.WalletExternalResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@HttpExchange("/api/v1")
public interface PaymentApiClient {

    @GetExchange("/wallets/owner/{ownerId}")
    Mono<WalletExternalResponse> getWalletByOwner(@PathVariable UUID ownerId);

    @PostExchange("/wallets")
    Mono<WalletExternalResponse> createWallet(@RequestBody WalletCreationRequest request);
}