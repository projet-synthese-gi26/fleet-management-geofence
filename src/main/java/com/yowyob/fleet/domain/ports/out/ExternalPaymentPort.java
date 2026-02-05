package com.yowyob.fleet.domain.ports.out;

import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.util.UUID;

public interface ExternalPaymentPort {
    /**
     * Récupère le solde d'un propriétaire.
     * Retourne BigDecimal.ZERO si le wallet n'existe pas encore (ou vide).
     */
    Mono<BigDecimal> getBalance(UUID ownerId);

    /**
     * Initialise un wallet pour un propriétaire.
     * Idempotent : Si existe déjà, retourne l'existant.
     */
    Mono<UUID> initializeWallet(UUID ownerId, String ownerName);
}