package com.yowyob.fleet.infrastructure.adapters.outbound.external;

import com.yowyob.fleet.domain.ports.out.ExternalPaymentPort;
import com.yowyob.fleet.infrastructure.adapters.outbound.external.client.PaymentApiClient;
import com.yowyob.fleet.infrastructure.adapters.outbound.external.dto.WalletCreationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentApiAdapter implements ExternalPaymentPort {

    private final PaymentApiClient paymentApiClient;

    @Override
    public Mono<BigDecimal> getBalance(UUID ownerId) {
        return paymentApiClient.getWalletByOwner(ownerId)
                .map(wallet -> wallet.balance())
                .onErrorResume(WebClientResponseException.NotFound.class, e -> {
                    log.info("Aucun wallet trouvé pour ownerId {}, solde supposé à 0", ownerId);
                    return Mono.just(BigDecimal.ZERO);
                })
                .onErrorResume(e -> {
                    log.error("Erreur lors de la récupération du solde pour {}: {}", ownerId, e.getMessage());
                    // En cas d'erreur technique, on propage l'erreur ou on gère un fallback selon le besoin métier
                    return Mono.error(e);
                });
    }

    @Override
    public Mono<UUID> initializeWallet(UUID ownerId, String ownerName) {
        return paymentApiClient.getWalletByOwner(ownerId)
                .map(wallet -> wallet.id()) // Si existe, on retourne son ID
                .onErrorResume(WebClientResponseException.NotFound.class, e -> {
                    // Si n'existe pas (404), on le crée
                    log.info("Création d'un nouveau wallet pour {}", ownerName);
                    return paymentApiClient.createWallet(new WalletCreationRequest(ownerId, ownerName))
                            .map(wallet -> wallet.id());
                });
    }
}