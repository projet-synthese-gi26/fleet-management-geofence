package com.yowyob.fleet.infrastructure.adapters.outbound.external;

import com.yowyob.fleet.domain.model.Vehicle;
import com.yowyob.fleet.domain.ports.out.ExternalVehiclePort;
import com.yowyob.fleet.infrastructure.adapters.outbound.external.client.VehicleApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class VehicleApiAdapter implements ExternalVehiclePort {

    private final VehicleApiClient vehicleApiClient;

    @Override
    public Mono<Vehicle> getExternalVehicleInfo(UUID vehicleId) {
        return vehicleApiClient.getVehicleById(vehicleId)
                .map(ext -> new Vehicle(
                        ext.id(), null, null, null,
                        ext.licensePlate(), ext.brand(), ext.model(),
                        ext.manufacturingYear(), ext.type(), ext.color(),
                        null, null, null, null, null
                ))
                .doOnError(e -> log.error("ERREUR CRITIQUE : Service VÃ©hicule Externe indisponible pour ID {}", vehicleId));
    }
}