package com.yowyob.fleet.application.service;

import com.yowyob.fleet.domain.exception.ManagerException;
import com.yowyob.fleet.domain.ports.in.ManageFleetManagerUseCase;
import com.yowyob.fleet.domain.ports.out.AuthPort;
import com.yowyob.fleet.domain.ports.out.FleetManagerPersistencePort;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.FleetManagerResponse;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.ManagerKpiResponse;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.DriverR2dbcRepository;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.FleetManagerR2dbcRepository;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.FleetR2dbcRepository;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.VehicleLocalR2dbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FleetManagerService implements ManageFleetManagerUseCase {

    private final FleetManagerR2dbcRepository managerRepository;
    private final FleetManagerPersistencePort managerPersistencePort;
    private final FleetR2dbcRepository fleetRepository;
    private final VehicleLocalR2dbcRepository vehicleRepository;
    private final DriverR2dbcRepository driverRepository;
    private final AuthPort authPort;

    private static final String SERVICE_NAME = "FLEET_MANAGEMENT";
    private static final String ROLE_MANAGER = "FLEET_MANAGER";

    @Override
    public Flux<FleetManagerResponse> getAllManagers(String token) {
        return authPort.getUsersByService(SERVICE_NAME, token)
            .filter(user -> user.roles() != null && user.roles().contains(ROLE_MANAGER))
            .flatMap(this::syncAndEnrich);
    }

    @Override
    public Mono<FleetManagerResponse> getManagerDetails(UUID userId, String token) {
        return authPort.getUserById(userId, token)
            .flatMap(this::syncAndEnrich);
    }

    private Mono<FleetManagerResponse> syncAndEnrich(AuthPort.UserDetail remoteUser) {
        return Mono.zip(
            managerRepository.findById(remoteUser.id())
                .switchIfEmpty(managerPersistencePort.createProfile(remoteUser.id(), "Société de " + remoteUser.lastName())
                    .then(managerRepository.findById(remoteUser.id()))),
            fleetRepository.countByManagerId(remoteUser.id()) // TÂCHE 7.1 : Chiffre réel
        ).map(tuple -> {
            var localEntity = tuple.getT1();
            var fleetCount = tuple.getT2();
            return new FleetManagerResponse(
                remoteUser.id(),
                remoteUser.firstName(),
                remoteUser.lastName(),
                remoteUser.email(),
                remoteUser.phone(),
                localEntity.getCompanyName(),
                "ACTIVE",
                fleetCount.intValue(), // Chiffre dynamisé
                remoteUser.photoUrl()
            );
        });
    }

    @Override
    public Mono<Void> updateManagerCompany(UUID userId, String companyName) {
        if (companyName == null || companyName.trim().isEmpty()) {
            return Mono.error(ManagerException.invalidCompanyData("Le nom ne peut pas être vide."));
        }
        return managerPersistencePort.updateCompany(userId, companyName);
    }

    
    @Override
    public Mono<ManagerKpiResponse> getManagerKpis(UUID managerId) {
        return Mono.zip(
            fleetRepository.countByManagerId(managerId),      // Flottes du manager
            vehicleRepository.countByManagerId(managerId),    // Véhicules du manager
            driverRepository.countByManagerId(managerId),     // Chauffeurs du manager (via JOIN)
            // Pour les trips actifs : compte les véhicules du manager qui ont le statut 'ON_TRIP'
            vehicleRepository.countByManagerIdAndStatus(managerId, "ON_TRIP") 
        ).map(t -> new ManagerKpiResponse(
            t.getT1(), // totalFleets
            t.getT2(), // totalVehicles
            t.getT3(), // totalDrivers
            t.getT4()  // activeTrips
        )).onErrorResume(e -> {
            log.error("Erreur calcul KPIs pour manager {}: {}", managerId, e.getMessage());
            return Mono.error(ManagerException.kpiCalculationFailed());
        });
    }

    @Override
    public Mono<Void> deleteManager(UUID userId, String token) {
        return authPort.deleteRemoteAccount(userId, token)
                .then(managerRepository.deleteById(userId));
    }
}