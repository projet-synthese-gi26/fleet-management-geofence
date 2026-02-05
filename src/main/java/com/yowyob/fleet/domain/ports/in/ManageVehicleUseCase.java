package com.yowyob.fleet.domain.ports.in;

import com.yowyob.fleet.domain.model.Vehicle;
import com.yowyob.fleet.domain.model.VehicleParameters;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.VehicleRequest;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Map;
import java.util.UUID;

public interface ManageVehicleUseCase {
    Mono<Vehicle> getVehicleDetails(UUID vehicleId, String token);
    
    // MODIFIÉ : Ajout du token pour la synchro lors du listing
    Flux<Vehicle> getVehicles(UUID requesterId, boolean isAdmin, String token); 

    Mono<Vehicle> createVehicle(UUID fleetId, VehicleRequest request, UUID managerId, String token);
    Mono<Vehicle> createIndependentVehicle(VehicleRequest request, UUID managerId, String token);

    Mono<Vehicle> updateVehicleInfo(UUID vehicleId, VehicleRequest request, String token);
    Mono<Vehicle> patchVehicleInfo(UUID vehicleId, Map<String, Object> updates, String token);
    
    Mono<Vehicle> uploadVinPhoto(UUID vehicleId, FilePart file, String token);
    Mono<Vehicle> deleteVinPhoto(UUID vehicleId, String token);
    
    Mono<Vehicle> uploadRegistrationPhoto(UUID vehicleId, FilePart file, String token);
    Mono<Vehicle> deleteRegistrationPhoto(UUID vehicleId, String token);
    
    Mono<Vehicle> addIllustrationImage(UUID vehicleId, FilePart file, String token);
    Mono<Vehicle> deleteIllustrationImage(UUID vehicleId, UUID imageId, String token);

    Mono<Vehicle> updateFinancialParameters(UUID vehicleId, VehicleParameters.Financial params, String token);
    Mono<Vehicle> updateMaintenanceParameters(UUID vehicleId, VehicleParameters.Maintenance params, String token);

    Mono<Void> removeVehicle(UUID vehicleId, String token);

    Flux<Map<String, Object>> getVehicleReferenceData(String resource, String token);
}