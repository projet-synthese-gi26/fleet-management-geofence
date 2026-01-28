package com.yowyob.fleet.infrastructure.adapters.outbound.external;

import com.yowyob.fleet.domain.model.Vehicle;
import com.yowyob.fleet.domain.ports.out.ExternalVehiclePort;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.VehicleRegistrationRequest;
import com.yowyob.fleet.infrastructure.adapters.outbound.external.client.VehicleApiClient;
import com.yowyob.fleet.infrastructure.adapters.outbound.external.dto.VehicleExternalResponse;
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
                .map(this::mapToDomain)
                .doOnError(e -> log.error("ERREUR CRITIQUE : Service Véhicule Externe indisponible pour ID {}", vehicleId));
    }

    @Override
    public Mono<Vehicle> createRemoteVehicle(VehicleRegistrationRequest req) {
        log.info("Appel distant : Création véhicule simplifié pour {}", req.licensePlate());
        
        // Utilisation de l'endpoint SIMPLIFIED avec query params
        return vehicleApiClient.createVehicleSimplified(
            req.brand(),                                // makeName
            req.model(),                                // modelName
            req.transmissionType() != null ? req.transmissionType() : "Manuelle",
            req.fuelType(),                             // fuelTypeName
            req.licensePlate(),                         // registrationNumber
            UUID.randomUUID().toString(),               // vehicleSerialNumber (Généré aléatoirement car obligatoire mais non saisi)
            null,                                       // registrationPhoto
            null,                                       // color (non supporté par simplified)
            req.brand()                                 // brand
        ).map(this::mapToDomain);
    }

    @Override
    public Mono<Void> deleteRemoteVehicle(UUID vehicleId) {
        return vehicleApiClient.deleteVehicle(vehicleId);
    }

    // Mapper interne : DTO Externe -> Objet Domaine
    private Vehicle mapToDomain(VehicleExternalResponse ext) {
        return new Vehicle(
                ext.vehicleId(), 
                null, // fleetId (inconnu ici)
                null, // driverId
                null, // typeId
                ext.registrationNumber(), 
                ext.brand(), 
                // Si le modèle est null dans la réponse, on remet la marque pour éviter des null pointer plus tard
                ext.brand(), 
                null, // year
                null, // type label
                null, // color
                null, // status
                ext.registrationPhoto(), 
                null, null, null // Paramètres
        );
    }
}