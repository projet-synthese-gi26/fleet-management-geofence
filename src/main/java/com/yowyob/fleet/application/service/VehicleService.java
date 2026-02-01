package com.yowyob.fleet.application.service;

import com.yowyob.fleet.domain.model.Vehicle;
import com.yowyob.fleet.domain.model.VehicleParameters;
import com.yowyob.fleet.domain.ports.in.ManageVehicleUseCase;
import com.yowyob.fleet.domain.ports.out.ExternalVehiclePort;
import com.yowyob.fleet.domain.ports.out.VehiclePersistencePort;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.VehicleRequest;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.VehicleTypeR2dbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Map;
import java.util.UUID;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleService implements ManageVehicleUseCase {

    private final VehiclePersistencePort localPersistencePort;
    private final ExternalVehiclePort externalVehiclePort;
    private final VehicleTypeR2dbcRepository vehicleTypeRepository;

    // --- LOGIQUE DE SYNCHRONISATION (COEUR DU SERVICE) ---

    private Mono<Vehicle> syncWithRemote(Vehicle remote, UUID vehicleId) {
        return localPersistencePort.getLocalDataById(vehicleId)
            .flatMap(local -> {
                // On crée un objet Vehicle fusionné (24 arguments)
                Vehicle updated = new Vehicle(
                    local.id(), local.fleetId(), local.managerId(), local.currentDriverId(), local.vehicleTypeId(),
                    remote.licensePlate(), remote.vehicleSerialNumber(), remote.brand(), remote.model(),
                    local.manufacturingYear(), remote.transmissionType(), remote.fuelType(),
                    remote.tankCapacity(), remote.totalSeatNumber(), remote.averageFuelConsumption(),
                    local.color(), local.status(), 
                    remote.photoUrl() != null ? remote.photoUrl() : local.photoUrl(),
                    remote.serialNumberPhotoUrl(), remote.registrationPhotoUrl(),
                    local.illustrationImages(), 
                    local.financialParameters(), local.maintenanceParameters(), local.operationalParameters()
                );
                return localPersistencePort.saveLocalData(updated);
            });
    }

    @Override
    public Mono<Vehicle> getVehicleDetails(UUID vehicleId, String token) {
        return externalVehiclePort.getExternalVehicleInfo(vehicleId, token)
                .flatMap(remote -> syncWithRemote(remote, vehicleId))
                .switchIfEmpty(localPersistencePort.getLocalDataById(vehicleId)); // Fallback local
    }

    @Override
    public Flux<Vehicle> getVehicles(UUID requesterId, boolean isAdmin, String token) {
        Flux<Vehicle> localStream = isAdmin ? 
                localPersistencePort.getAllVehicles() : 
                localPersistencePort.getVehiclesByManager(requesterId);

        return localStream.flatMap(v -> 
            getVehicleDetails(v.id(), token)
                .onErrorResume(e -> Mono.just(v)) // Si erreur réseau, on garde la version locale
        );
    }

    // --- CRÉATION ---

    @Override
    public Mono<Vehicle> createVehicle(UUID fleetId, VehicleRequest request, UUID managerId, String token) {
        return createVehicleInternal(fleetId, request, managerId, token);
    }

    @Override
    public Mono<Vehicle> createIndependentVehicle(VehicleRequest request, UUID managerId, String token) {
        return createVehicleInternal(null, request, managerId, token);
    }

    @Transactional
    protected Mono<Vehicle> createVehicleInternal(UUID fleetId, VehicleRequest req, UUID managerId, String token) {
        return vehicleTypeRepository.existsById(req.vehicleTypeId())
                .flatMap(exists -> {
                    if (!exists) return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type invalide"));
                    return externalVehiclePort.createRemoteVehicle(req, token)
                            .flatMap(remote -> {
                                Vehicle shell = new Vehicle(remote.id(), fleetId, managerId, null, req.vehicleTypeId(),
                                        req.licensePlate(), remote.vehicleSerialNumber(), remote.brand(), remote.model(),
                                        req.manufacturingYear(), req.transmissionType(), req.fuelType(), 
                                        req.tankCapacity(), req.totalSeatNumber(), req.averageFuelConsumption(), 
                                        req.color(), "AVAILABLE", remote.photoUrl(), 
                                        null, null, Collections.emptyList(), null, null, null);
                                return localPersistencePort.saveLocalData(shell);
                            });
                })
                .flatMap(v -> getVehicleDetails(v.id(), token));
    }

    // --- MODIFICATION ---

    @Override
    @Transactional
    public Mono<Vehicle> updateVehicleInfo(UUID vehicleId, VehicleRequest request, String token) {
        return externalVehiclePort.updateRemoteVehicle(vehicleId, request, token)
                .flatMap(remote -> syncWithRemote(remote, vehicleId))
                .then(getVehicleDetails(vehicleId, token));
    }

    @Override
    @Transactional
    public Mono<Vehicle> patchVehicleInfo(UUID vehicleId, Map<String, Object> updates, String token) {
        return externalVehiclePort.patchRemoteVehicle(vehicleId, updates, token)
                .flatMap(remote -> syncWithRemote(remote, vehicleId))
                .then(getVehicleDetails(vehicleId, token));
    }

    // --- PARAMÈTRES (Sync après modification locale) ---

    @Override
    public Mono<Vehicle> updateFinancialParameters(UUID vehicleId, VehicleParameters.Financial params, String token) {
        return localPersistencePort.getLocalDataById(vehicleId)
                .flatMap(v -> {
                    Vehicle toSave = new Vehicle(
                        v.id(), v.fleetId(), v.managerId(), v.currentDriverId(), v.vehicleTypeId(),
                        v.licensePlate(), v.vehicleSerialNumber(), v.brand(), v.model(), v.manufacturingYear(),
                        v.transmissionType(), v.fuelType(), v.tankCapacity(), v.totalSeatNumber(), v.averageFuelConsumption(),
                        v.color(), v.status(), v.photoUrl(), v.serialNumberPhotoUrl(), v.registrationPhotoUrl(),
                        v.illustrationImages(), params, v.maintenanceParameters(), v.operationalParameters());
                    return localPersistencePort.saveLocalData(toSave);
                })
                .then(getVehicleDetails(vehicleId, token));
    }

    @Override
    public Mono<Vehicle> updateMaintenanceParameters(UUID vehicleId, VehicleParameters.Maintenance params, String token) {
        return localPersistencePort.getLocalDataById(vehicleId)
                .flatMap(v -> {
                    Vehicle toSave = new Vehicle(
                        v.id(), v.fleetId(), v.managerId(), v.currentDriverId(), v.vehicleTypeId(),
                        v.licensePlate(), v.vehicleSerialNumber(), v.brand(), v.model(), v.manufacturingYear(),
                        v.transmissionType(), v.fuelType(), v.tankCapacity(), v.totalSeatNumber(), v.averageFuelConsumption(),
                        v.color(), v.status(), v.photoUrl(), v.serialNumberPhotoUrl(), v.registrationPhotoUrl(),
                        v.illustrationImages(), v.financialParameters(), params, v.operationalParameters());
                    return localPersistencePort.saveLocalData(toSave);
                })
                .then(getVehicleDetails(vehicleId, token));
    }

    // --- MÉDIAS (Délégués au service Media, mais synchronisés ici) ---

    @Override
    public Mono<Vehicle> uploadVinPhoto(UUID vehicleId, FilePart file, String token) {
        return externalVehiclePort.uploadDocument(vehicleId, "serial", file, token)
                .then(getVehicleDetails(vehicleId, token));
    }

    @Override
    public Mono<Vehicle> deleteVinPhoto(UUID vehicleId, String token) {
        return externalVehiclePort.deleteDocument(vehicleId, "serial", token)
                .then(getVehicleDetails(vehicleId, token));
    }

    @Override
    public Mono<Vehicle> uploadRegistrationPhoto(UUID vehicleId, FilePart file, String token) {
        return externalVehiclePort.uploadDocument(vehicleId, "registration", file, token)
                .then(getVehicleDetails(vehicleId, token));
    }

    @Override
    public Mono<Vehicle> deleteRegistrationPhoto(UUID vehicleId, String token) {
        return externalVehiclePort.deleteDocument(vehicleId, "registration", token)
                .then(getVehicleDetails(vehicleId, token));
    }

    @Override
    public Mono<Vehicle> addIllustrationImage(UUID vehicleId, FilePart file, String token) {
        return externalVehiclePort.addImage(vehicleId, file, token)
                .then(getVehicleDetails(vehicleId, token));
    }

    @Override
    public Mono<Vehicle> deleteIllustrationImage(UUID vehicleId, UUID imageId, String token) {
        return externalVehiclePort.deleteImage(imageId.toString(), token)
                .then(getVehicleDetails(vehicleId, token));
    }

    @Override
    public Mono<Void> removeVehicle(UUID vehicleId, String token) {
        return externalVehiclePort.deleteRemoteVehicle(vehicleId, token)
                .onErrorResume(e -> Mono.empty())
                .then(localPersistencePort.deleteLocalData(vehicleId));
    }

    @Override
    public Flux<Map<String, Object>> getVehicleReferenceData(String resource, String token) {
        return externalVehiclePort.getReferenceData(resource, token);
    }
}