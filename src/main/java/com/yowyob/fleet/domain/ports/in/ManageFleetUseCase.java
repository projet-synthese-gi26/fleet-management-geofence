package com.yowyob.fleet.domain.ports.in;

import com.yowyob.fleet.domain.model.Fleet;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.FleetStatsResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface ManageFleetUseCase {
    Mono<Fleet> createFleet(Fleet fleet, UUID managerId);
    Mono<Fleet> getFleetById(UUID fleetId, UUID requesterId, boolean isAdmin);
    Flux<Fleet> getFleets(UUID requesterId, boolean isAdmin);
    Mono<Fleet> updateFleet(UUID fleetId, Fleet fleet, UUID requesterId, boolean isAdmin);
    Mono<Void> deleteFleet(UUID fleetId, UUID requesterId, boolean isAdmin);

    // --- AJOUT TÂCHE 6.2 ---
    Mono<FleetStatsResponse> getFleetStatistics(UUID fleetId, UUID requesterId, boolean isAdmin);
}