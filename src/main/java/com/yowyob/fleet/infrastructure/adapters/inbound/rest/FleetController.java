package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import com.yowyob.fleet.domain.model.Fleet;
import com.yowyob.fleet.domain.ports.in.ManageFleetUseCase;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.FleetRequest;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.FleetResponse;
import com.yowyob.fleet.infrastructure.mappers.FleetMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fleets")
@RequiredArgsConstructor
@Tag(name = "07. Fleets", description = "Gestion des flottes")
public class FleetController {

    private final ManageFleetUseCase fleetUseCase;
    private final FleetMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new fleet")
    public Mono<FleetResponse> create(@Valid @RequestBody FleetRequest request) {
        return fleetUseCase.createFleet(mapper.toDomain(request))
                .map(mapper::toResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get fleet details by ID")
    public Mono<FleetResponse> getById(@PathVariable UUID id) {
        return fleetUseCase.getFleetById(id)
                .map(mapper::toResponse);
    }

    @GetMapping
    @Operation(summary = "List all fleets")
    public Flux<FleetResponse> getAll() {
        return fleetUseCase.getAllFleets()
                .map(mapper::toResponse);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Full update of a fleet")
    public Mono<FleetResponse> update(@PathVariable UUID id, @Valid @RequestBody FleetRequest request) {
        return fleetUseCase.updateFleet(id, mapper.toDomain(request))
                .map(mapper::toResponse);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partial update of a fleet")
    public Mono<FleetResponse> patch(@PathVariable UUID id, @RequestBody FleetRequest request) {
        return fleetUseCase.patchFleet(id, mapper.toDomain(request))
                .map(mapper::toResponse);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a fleet")
    public Mono<Void> delete(@PathVariable UUID id) {
        return fleetUseCase.deleteFleet(id);
    }
}