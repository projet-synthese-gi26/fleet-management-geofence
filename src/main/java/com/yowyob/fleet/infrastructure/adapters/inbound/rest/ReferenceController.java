package com.yowyob.fleet.infrastructure.adapters.inbound.rest;

import com.yowyob.fleet.application.service.VehicleTypeService;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.VehicleTypeEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/references")
@RequiredArgsConstructor
@Tag(name = "10. Références", description = "Données statiques (Types de véhicules, etc.)")
public class ReferenceController {

    private final VehicleTypeService vehicleTypeService;

    @GetMapping("/vehicle-types")
    @Operation(summary = "Lister les types de véhicules disponibles", 
               description = "Utilisez l'ID retourné ici pour créer un véhicule.")
    public Flux<VehicleTypeEntity> getVehicleTypes() {
        return vehicleTypeService.getAllTypes();
    }
}