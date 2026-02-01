package com.yowyob.fleet.domain.ports.out;

import com.yowyob.fleet.domain.model.Vehicle;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.VehicleRegistrationRequest;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Map;
import java.util.UUID;

public interface ExternalVehiclePort {
    Mono<Vehicle> getExternalVehicleInfo(UUID vehicleId, String token);
    
    Mono<Vehicle> createRemoteVehicleSimplified(VehicleRegistrationRequest request, String token);
    
    Mono<Vehicle> updateRemoteVehicle(UUID vehicleId, VehicleRegistrationRequest request, String token);
    Mono<Vehicle> patchRemoteVehicle(UUID vehicleId, String brand, String token); 
    Mono<Void> deleteRemoteVehicle(UUID vehicleId, String token);

    // Médias
    Mono<Void> uploadDocument(UUID vehicleId, String docType, FilePart file, String token);
    Mono<Void> deleteDocument(UUID vehicleId, String docType, String token);
    
    Mono<String> addImage(UUID vehicleId, FilePart file, String token);
    Flux<String> getImages(UUID vehicleId, String token); 
    Mono<Void> deleteImage(String imageId, String token); 

    // --- NOUVEAU : Référentiels ---
    Flux<Map<String, Object>> getReferenceData(String resource, String token);

    Mono<Void> assignDriverRemote(UUID vehicleId, UUID driverId, String token);
}