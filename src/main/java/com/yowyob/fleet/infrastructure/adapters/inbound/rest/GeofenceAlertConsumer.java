package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import com.yowyob.fleet.domain.ports.in.ManageGeofenceUseCase;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.GeofenceAlertMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeofenceAlertConsumer {

    private final ManageGeofenceUseCase geofenceUseCase;

    /**
     * Ã‰coute le topic dÃ©fini dans application.yml : application.kafka.topics.geofence-alerts
     */
    @KafkaListener(
        topics = "${application.kafka.topics.geofence-alerts}", 
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeAlert(GeofenceAlertMessage message) {
        log.debug("ðŸ“© Payload Kafka reÃ§u : {}", message);

        if (message.vehicleId() == null || message.zoneId() == null) {
            log.warn("âš ï¸  Alerte Kafka ignorÃ©e : IDs manquants.");
            return;
        }

        // On dÃ©clenche le flux rÃ©actif
        // Note : Dans un contexte @KafkaListener non-rÃ©actif (Spring Kafka standard), 
        // on doit souscrire manuellement pour exÃ©cuter le Mono.
        // geofenceUseCase.handleIncomingAlert(
        //         message.vehicleId(),
        //         message.zoneId(),
        //         message.type(),
        //         message.timestamp()
        // ).subscribe(
        //     success -> {}, // On ne fait rien au succÃ¨s (dÃ©jÃ  loggÃ© dans le service)
        //     error -> log.error("â Œ Erreur critique processing Kafka : ", error)
        // );
    }
}