package com.yowyob.fleet.application.service;

import com.yowyob.fleet.domain.exception.VehicleException;
import com.yowyob.fleet.domain.model.Vehicle;
import com.yowyob.fleet.domain.model.VehicleParameters;
import com.yowyob.fleet.domain.ports.in.ManageVehicleUseCase;
import com.yowyob.fleet.domain.ports.out.ExternalGeofencePort;
import com.yowyob.fleet.domain.ports.out.ExternalVehiclePort;
import com.yowyob.fleet.domain.ports.out.VehiclePersistencePort;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.VehicleRequest;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleService implements ManageVehicleUseCase {

    private final VehiclePersistencePort localPersistencePort;
    private final ExternalVehiclePort externalVehiclePort;
    private final ExternalGeofencePort geofencePort; 
    private final VehicleTypeR2dbcRepository vehicleTypeRepo;
    private final ManufacturerR2dbcRepository mfrRepo;
    private final FuelTypeR2dbcRepository fuelRepo;
    private final OperationalParameterR2dbcRepository operationalRepo;

    // ========================================================================
    // --- 09a. GESTION DU PARC (CONTRAT MANAGER) ---
    // ========================================================================

    @Override
    public Mono<Vehicle> getVehicleDetails(UUID vehicleId, String token) {
        return externalVehiclePort.getExternalVehicleInfo(vehicleId, token)
                .flatMap(remote -> syncLocalCache(remote, vehicleId))
                .switchIfEmpty(localPersistencePort.getLocalDataById(vehicleId))
                .switchIfEmpty(Mono.error(VehicleException.notFound(vehicleId)));
    }

    @Override
    public Flux<Vehicle> getVehicles(UUID requesterId, boolean isAdmin, String token) {
        Flux<Vehicle> localStream = isAdmin ? 
                localPersistencePort.getAllVehicles() : 
                localPersistencePort.getVehiclesByManager(requesterId);

        return localStream.flatMap(v -> getVehicleDetails(v.id(), token)
                .onErrorResume(e -> Mono.just(v))); 
    }

    @Override
    @Transactional
    public Mono<Vehicle> createIndependentVehicle(VehicleRequest request, UUID managerId, String token) {
        return createVehicle(null, request, managerId, token);
    }

    @Override
    @Transactional
    public Mono<Vehicle> createVehicle(UUID fleetId, VehicleRequest req, UUID managerId, String token) {
        return vehicleTypeRepo.existsById(req.vehicleTypeId())
            .flatMap(exists -> {
                if (!exists) return Mono.error(VehicleException.invalidVehicleType());
                
                return externalVehiclePort.createRemoteVehicle(req, token)
                    .flatMap(remote -> {
                        Vehicle shell = new Vehicle(
                            remote.id(), fleetId, managerId, null, req.vehicleTypeId(),
                            req.licensePlate(), remote.vehicleSerialNumber(), req.brand(), req.model(),
                            req.manufacturingYear(), req.transmissionType(), req.fuelType(), 
                            req.tankCapacity(), req.totalSeatNumber(), req.averageFuelConsumption(), 
                            req.color(), "AVAILABLE", remote.photoUrl(), 
                            null, null, Collections.emptyList(), null, null, null, null); 
                        
                        return localPersistencePort.saveLocalData(shell);
                    })
                    .flatMap(savedLocal -> {
                        log.info("📡 Enregistrement du véhicule {} dans le moteur Geofence", savedLocal.licensePlate());
                        return geofencePort.registerVehicleAndAssignToZone(savedLocal, null, "POLYGON")
                                .thenReturn(savedLocal)
                                .onErrorResume(e -> {
                                    log.warn("⚠️ Synchro Geofence échouée pour {}: {}", savedLocal.licensePlate(), e.getMessage());
                                    return Mono.just(savedLocal);
                                });
                    });
            }).flatMap(v -> getVehicleDetails(v.id(), token));
    }

    @Override
    @Transactional
    public Mono<Vehicle> patchVehicleInfo(UUID id, Map<String, Object> u, String t) {
        return externalVehiclePort.patchRemoteVehicle(id, u, t)
                .flatMap(r -> syncLocalCache(r, id));
    }

    @Override
    @Transactional
    public Mono<Vehicle> updateFinancialParameters(UUID id, VehicleParameters.Financial p, String t) { 
        return localPersistencePort.getLocalDataById(id)
                .flatMap(v -> {
                    Vehicle updated = new Vehicle(
                        v.id(), v.fleetId(), v.managerId(), v.currentDriverId(), v.vehicleTypeId(),
                        v.licensePlate(), v.vehicleSerialNumber(), v.brand(), v.model(),
                        v.manufacturingYear(), v.transmissionType(), v.fuelType(),
                        v.tankCapacity(), v.totalSeatNumber(), v.averageFuelConsumption(),
                        v.color(), v.status(), v.photoUrl(), v.serialNumberPhotoUrl(),
                        v.registrationPhotoUrl(), v.illustrationImages(), p, v.maintenanceParameters(), 
                        v.operationalParameters(), v.geofenceRemoteId()
                    );
                    return localPersistencePort.saveLocalData(updated);
                }).then(getVehicleDetails(id, t));
    }

    @Override
    @Transactional
    public Mono<Vehicle> updateMaintenanceParameters(UUID id, VehicleParameters.Maintenance p, String t) {
        return localPersistencePort.getLocalDataById(id)
                .flatMap(v -> {
                    Vehicle updated = new Vehicle(
                        v.id(), v.fleetId(), v.managerId(), v.currentDriverId(), v.vehicleTypeId(),
                        v.licensePlate(), v.vehicleSerialNumber(), v.brand(), v.model(),
                        v.manufacturingYear(), v.transmissionType(), v.fuelType(),
                        v.tankCapacity(), v.totalSeatNumber(), v.averageFuelConsumption(),
                        v.color(), v.status(), v.photoUrl(), v.serialNumberPhotoUrl(),
                        v.registrationPhotoUrl(), v.illustrationImages(), v.financialParameters(), p, 
                        v.operationalParameters(), v.geofenceRemoteId()
                    );
                    return localPersistencePort.saveLocalData(updated);
                }).then(getVehicleDetails(id, t));
    }

    @Override
    public Mono<Void> removeVehicle(UUID id, String t) {
        return externalVehiclePort.deleteRemoteVehicle(id, t)
                .then(localPersistencePort.deleteLocalData(id));
    }

    // ========================================================================
    // --- 09c. OPÉRATIONNEL (CONTRAT DRIVER) ---
    // ========================================================================

    @Override
    public Mono<VehicleParameters.Operational> getOperationalData(UUID vehicleId) {
        return operationalRepo.findByVehicleId(vehicleId)
                .map(e -> new VehicleParameters.Operational(
                        e.getStatut(), 
                        e.getCurrentSpeed() != null ? e.getCurrentSpeed().floatValue() : 0.0f, 
                        e.getFuelLevel(),
                        e.getMileage() != null ? e.getMileage().floatValue() : 0.0f, 
                        e.getOdometerReading() != null ? e.getOdometerReading().floatValue() : 0.0f,
                        e.getBearing() != null ? e.getBearing().floatValue() : 0.0f, 
                        e.getTimestamp(), 
                        null
                ));
    }

    @Override
    @Transactional
    public Mono<Void> updateOperationalData(UUID vehicleId, Map<String, Object> updates) {
        return operationalRepo.findByVehicleId(vehicleId)
                .flatMap(e -> {
                    if (updates.containsKey("fuelLevel")) e.setFuelLevel(updates.get("fuelLevel").toString());
                    if (updates.containsKey("odometerReading")) {
                        e.setOdometerReading(new BigDecimal(updates.get("odometerReading").toString()));
                    }
                    e.setTimestamp(Instant.now());
                    return operationalRepo.save(e);
                }).then();
    }

    // ========================================================================
    // --- 09d. RÉFÉRENTIELS (CONTRAT LOOKUP LOCAL) ---
    // ========================================================================

    @Override
    public Flux<Map<String, Object>> getLocalLookupData(String resource) {
        return switch (resource.toLowerCase()) {
            case "vehicle-types" -> vehicleTypeRepo.findAll().map(t -> Map.of("id", t.getId(), "label", t.getLabel(), "code", t.getCode()));
            case "manufacturers" -> mfrRepo.findAll().map(m -> Map.of("id", m.getId(), "label", m.getLabel(), "code", m.getCode()));
            case "fuel-types" -> fuelRepo.findAll().map(f -> Map.of("id", f.getId(), "label", f.getLabel(), "code", f.getCode()));
            default -> Flux.error(VehicleException.invalidResource());
        };
    }

    // ========================================================================
    // --- LOGIQUE DE SYNCHRONISATION DU CACHE ---
    // ========================================================================

    private Mono<Vehicle> syncLocalCache(Vehicle remote, UUID vehicleId) {
        return localPersistencePort.getLocalDataById(vehicleId)
                .flatMap(local -> {
                    Vehicle updated = new Vehicle(
                        local.id(), local.fleetId(), local.managerId(), local.currentDriverId(), local.vehicleTypeId(),
                        remote.licensePlate(), remote.vehicleSerialNumber(), remote.brand(), remote.model(),
                        local.manufacturingYear(), remote.transmissionType(), remote.fuelType(),
                        remote.tankCapacity(), remote.totalSeatNumber(), remote.averageFuelConsumption(),
                        local.color(), local.status(), remote.photoUrl(),
                        remote.serialNumberPhotoUrl(), remote.registrationPhotoUrl(),
                        local.illustrationImages(), local.financialParameters(), 
                        local.maintenanceParameters(), local.operationalParameters(),
                        local.geofenceRemoteId() 
                    );
                    return localPersistencePort.saveLocalData(updated);
                });
    }
}