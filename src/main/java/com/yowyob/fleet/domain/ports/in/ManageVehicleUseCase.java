package com.yowyob.fleet.domain.ports.in;

import com.yowyob.fleet.domain.model.Vehicle;
import com.yowyob.fleet.domain.model.VehicleParameters;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.VehicleRegistrationRequest;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Map;
import java.util.UUID;

public interface ManageVehicleUseCase {
    Mono<Vehicle> getVehicleDetails(UUID vehicleId, String token);
    Flux<Vehicle> getMyVehicles(UUID managerId); 

    Mono<Vehicle> createVehicle(UUID fleetId, VehicleRegistrationRequest request, UUID managerId, String token);
    Mono<Vehicle> createIndependentVehicle(VehicleRegistrationRequest request, UUID managerId, String token);

    Mono<Vehicle> updateVehicleInfo(UUID vehicleId, VehicleRegistrationRequest request, String token);
    Mono<Vehicle> patchVehicleInfo(UUID vehicleId, String brand, String model, String token);
    
    Mono<Void> uploadVinPhoto(UUID vehicleId, FilePart file, String token);
    Mono<Void> deleteVinPhoto(UUID vehicleId, String token);
    
    Mono<Void> uploadRegistrationPhoto(UUID vehicleId, FilePart file, String token);
    Mono<Void> deleteRegistrationPhoto(UUID vehicleId, String token);
    
    Mono<Void> addIllustrationImage(UUID vehicleId, FilePart file, String token);
    Flux<String> getIllustrationImages(UUID vehicleId, String token);
    Mono<Void> deleteIllustrationImage(UUID imageId, String token);

    Mono<Void> updateFinancialParameters(UUID vehicleId, VehicleParameters.Financial params);
    Mono<Void> updateMaintenanceParameters(UUID vehicleId, VehicleParameters.Maintenance params);
    Mono<Void> removeVehicle(UUID vehicleId, String token);

    // --- NOUVEAU ---
    Flux<Map<String, Object>> getVehicleReferenceData(String resource, String token);
}