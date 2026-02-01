package com.yowyob.fleet.application.service;

import com.yowyob.fleet.domain.model.Vehicle;
import com.yowyob.fleet.domain.model.VehicleParameters;
import com.yowyob.fleet.domain.ports.in.ManageVehicleUseCase;
import com.yowyob.fleet.domain.ports.out.ExternalVehiclePort;
import com.yowyob.fleet.domain.ports.out.VehiclePersistencePort;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.VehicleRegistrationRequest;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.VehicleTypeR2dbcRepository; // <--- AJOUT
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException; // <--- AJOUT
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleService implements ManageVehicleUseCase {

    private final VehiclePersistencePort localPersistencePort;
    private final ExternalVehiclePort externalVehiclePort;
    private final VehicleTypeR2dbcRepository vehicleTypeRepository; // <--- INJECTION

    @Override
    public Mono<Vehicle> getVehicleDetails(UUID vehicleId, String token) {
        return Mono.zip(
                localPersistencePort.getLocalDataById(vehicleId),
                externalVehiclePort.getExternalVehicleInfo(vehicleId, token) 
        ).map(tuple -> merge(tuple.getT1(), tuple.getT2()));
    }

    @Override
    public Flux<Vehicle> getMyVehicles(UUID managerId) {
        return localPersistencePort.getVehiclesByManager(managerId);
    }

    @Override
    public Mono<Vehicle> createVehicle(UUID fleetId, VehicleRegistrationRequest request, UUID managerId, String token) {
        return createVehicleInternal(fleetId, request, managerId, token);
    }

    @Override
    public Mono<Vehicle> createIndependentVehicle(VehicleRegistrationRequest request, UUID managerId, String token) {
        return createVehicleInternal(null, request, managerId, token);
    }

    @Transactional
    protected Mono<Vehicle> createVehicleInternal(UUID fleetId, VehicleRegistrationRequest req, UUID managerId, String token) {
        // --- 1. VALIDATION PRÉALABLE ---
        // On vérifie que le type existe AVANT d'appeler le service distant
        return vehicleTypeRepository.existsById(req.vehicleTypeId())
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Type de véhicule invalide ou inexistant : " + req.vehicleTypeId()));
                }
                
                // --- 2. EXECUTION ---
                log.info("Création véhicule Distant [Plaque: {}] - Manager: {}", req.registrationNumber(), managerId);
                
                return externalVehiclePort.createRemoteVehicleSimplified(req, token)
                    .flatMap(remoteVehicle -> {
                        log.info("Véhicule distant créé avec ID: {}", remoteVehicle.id());
                        
                        Vehicle localShell = new Vehicle(
                            remoteVehicle.id(),
                            fleetId,
                            managerId,
                            null, 
                            req.vehicleTypeId(),
                            req.registrationNumber(), 
                            remoteVehicle.vehicleSerialNumber(),
                            remoteVehicle.brand(),
                            remoteVehicle.model(),
                            remoteVehicle.manufacturingYear(),
                            remoteVehicle.transmissionType(),
                            remoteVehicle.fuelType(),
                            remoteVehicle.tankCapacity(),
                            remoteVehicle.totalSeatNumber(),
                            remoteVehicle.averageFuelConsumption(),
                            remoteVehicle.color(),
                            "AVAILABLE",
                            remoteVehicle.photoUrl(),
                            null, null, 
                            null, null, null 
                        );

                        return localPersistencePort.saveLocalData(localShell)
                                .map(savedLocal -> merge(savedLocal, remoteVehicle));
                    });
            });
    }

    @Override
    public Mono<Vehicle> updateVehicleInfo(UUID vehicleId, VehicleRegistrationRequest request, String token) {
        return externalVehiclePort.updateRemoteVehicle(vehicleId, request, token) 
                .flatMap(remote -> localPersistencePort.getLocalDataById(vehicleId)
                        .map(local -> merge(local, remote)));
    }

    @Override
    public Mono<Vehicle> patchVehicleInfo(UUID vehicleId, String brand, String model, String token) {
        return externalVehiclePort.patchRemoteVehicle(vehicleId, brand, token) 
                .flatMap(remote -> localPersistencePort.getLocalDataById(vehicleId)
                        .map(local -> merge(local, remote)));
    }

    // --- MEDIAS (Pas de changement) ---

    @Override
    public Mono<Void> uploadVinPhoto(UUID vehicleId, FilePart file, String token) {
        return externalVehiclePort.uploadDocument(vehicleId, "serial", file, token);
    }

    @Override
    public Mono<Void> deleteVinPhoto(UUID vehicleId, String token) {
        return externalVehiclePort.deleteDocument(vehicleId, "serial", token);
    }

    @Override
    public Mono<Void> uploadRegistrationPhoto(UUID vehicleId, FilePart file, String token) {
        return externalVehiclePort.uploadDocument(vehicleId, "registration", file, token);
    }

    @Override
    public Mono<Void> deleteRegistrationPhoto(UUID vehicleId, String token) {
        return externalVehiclePort.deleteDocument(vehicleId, "registration", token);
    }

    @Override
    public Mono<Void> addIllustrationImage(UUID vehicleId, FilePart file, String token) {
        return externalVehiclePort.addImage(vehicleId, file, token).then();
    }

    @Override
    public Flux<String> getIllustrationImages(UUID vehicleId, String token) {
        return externalVehiclePort.getImages(vehicleId, token);
    }

    @Override
    public Mono<Void> deleteIllustrationImage(UUID imageId, String token) {
        return externalVehiclePort.deleteImage(imageId.toString(), token);
    }

    // --- INTERNE (Pas de changement) ---

    @Override
    public Mono<Void> updateFinancialParameters(UUID vehicleId, VehicleParameters.Financial params) {
        return localPersistencePort.getLocalDataById(vehicleId)
                .flatMap(v -> saveWithNewParams(v, params, v.maintenanceParameters()));
    }

    @Override
    public Mono<Void> updateMaintenanceParameters(UUID vehicleId, VehicleParameters.Maintenance params) {
        return localPersistencePort.getLocalDataById(vehicleId)
                .flatMap(v -> saveWithNewParams(v, v.financialParameters(), params));
    }

    @Override
    public Mono<Void> removeVehicle(UUID vehicleId, String token) {
        return externalVehiclePort.deleteRemoteVehicle(vehicleId, token) 
                .onErrorResume(e -> Mono.empty())
                .then(localPersistencePort.deleteLocalData(vehicleId));
    }

    private Vehicle merge(Vehicle local, Vehicle remote) {
        return new Vehicle(
                local.id(),
                local.fleetId(),
                local.managerId(),
                local.currentDriverId(),
                local.vehicleTypeId(),
                remote.licensePlate(),
                remote.vehicleSerialNumber(),
                remote.brand(),
                remote.model(),
                remote.manufacturingYear(),
                remote.transmissionType(),
                remote.fuelType(),
                remote.tankCapacity(),
                remote.totalSeatNumber(),
                remote.averageFuelConsumption(),
                remote.color(),
                local.status(),
                remote.photoUrl() != null ? remote.photoUrl() : local.photoUrl(),
                remote.serialNumberPhotoUrl(),
                remote.registrationPhotoUrl(),
                local.financialParameters(),
                local.maintenanceParameters(),
                null
        );
    }

    private Mono<Void> saveWithNewParams(Vehicle v, VehicleParameters.Financial fin, VehicleParameters.Maintenance maint) {
        Vehicle toSave = new Vehicle(
            v.id(), v.fleetId(), v.managerId(), v.currentDriverId(), v.vehicleTypeId(),
            v.licensePlate(), v.vehicleSerialNumber(), v.brand(), v.model(), v.manufacturingYear(),
            v.transmissionType(), v.fuelType(), v.tankCapacity(), v.totalSeatNumber(), v.averageFuelConsumption(),
            v.color(), v.status(), v.photoUrl(), v.serialNumberPhotoUrl(), v.registrationPhotoUrl(),
            fin, maint, v.operationalParameters()
        );
        return localPersistencePort.saveLocalData(toSave).then();
    }
}