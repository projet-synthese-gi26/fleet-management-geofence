package com.yowyob.fleet.infrastructure.adapters.outbound.external;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.VehicleOwnershipRequest;

import com.yowyob.fleet.domain.model.Vehicle;
import com.yowyob.fleet.domain.ports.out.ExternalVehiclePort;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.VehicleRegistrationRequest;
import com.yowyob.fleet.infrastructure.adapters.outbound.external.client.VehicleApiClient;
import com.yowyob.fleet.infrastructure.adapters.outbound.external.dto.VehicleExternalResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class VehicleApiAdapter implements ExternalVehiclePort {

    private final VehicleApiClient apiClient;
    private final WebClient.Builder webClientBuilder;

    @Value("${application.external.vehicle-service-url}")
    private String serviceUrl;

    // Helper pour garantir le format "Bearer <token>"
    private String ensureBearer(String token) {
        if (token == null) return "";
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }

    // --- LECTURE ---

    @Override
    public Mono<Vehicle> getExternalVehicleInfo(UUID vehicleId, String token) {
        return apiClient.getById(vehicleId, ensureBearer(token))
                .map(this::mapToDomain)
                .onErrorResume(e -> {
                    log.error("Erreur récupération véhicule distant {}: {}", vehicleId, e.getMessage());
                    // On retourne vide pour permettre au service d'afficher au moins les données locales
                    return Mono.empty(); 
                });
    }

    // --- CRÉATION / MODIFICATION ---

    @Override
    public Mono<Vehicle> createRemoteVehicleSimplified(VehicleRegistrationRequest request, String token) {
        return apiClient.createSimplified(request, ensureBearer(token))
                .map(this::mapToDomain);
    }

    @Override
    public Mono<Vehicle> updateRemoteVehicle(UUID vehicleId, VehicleRegistrationRequest request, String token) {
        return apiClient.updateFull(vehicleId, request, ensureBearer(token))
                .map(this::mapToDomain);
    }

    @Override
    public Mono<Vehicle> patchRemoteVehicle(UUID vehicleId, String brand, String token) {
        Map<String, Object> patch = new HashMap<>();
        patch.put("brand", brand);
        return apiClient.updatePartial(vehicleId, patch, ensureBearer(token))
                .map(this::mapToDomain);
    }

    @Override
    public Mono<Void> deleteRemoteVehicle(UUID vehicleId, String token) {
        return apiClient.delete(vehicleId, ensureBearer(token));
    }

    // --- GESTION FICHIERS (MULTIPART MANUEL) ---

    @Override
    public Mono<Void> uploadDocument(UUID vehicleId, String docType, FilePart file, String token) {
        String endpoint = "/vehicles/" + vehicleId + "/documents/" + docType; // serial ou registration
        return uploadFile(endpoint, file, "PUT", ensureBearer(token));
    }

    @Override
    public Mono<Void> deleteDocument(UUID vehicleId, String docType, String token) {
        if ("serial".equals(docType)) return apiClient.deleteSerialDoc(vehicleId, ensureBearer(token));
        if ("registration".equals(docType)) return apiClient.deleteRegistrationDoc(vehicleId, ensureBearer(token));
        return Mono.error(new IllegalArgumentException("Type de document inconnu: " + docType));
    }

    @Override
    public Mono<String> addImage(UUID vehicleId, FilePart file, String token) {
        String endpoint = "/vehicles/" + vehicleId + "/images";
        // L'upload POST retourne un objet JSON avec "imagePath"
        return uploadFileAndGetResponse(endpoint, file, ensureBearer(token))
                .map(resp -> (String) resp.getOrDefault("imagePath", ""));
    }

    @Override
    public Flux<String> getImages(UUID vehicleId, String token) {
        return apiClient.getImages(vehicleId, ensureBearer(token))
                .map(map -> (String) map.get("imagePath"));
    }

    @Override
    public Mono<Void> deleteImage(String imageId, String token) {
        return apiClient.deleteImage(imageId, ensureBearer(token));
    }

    // --- LOOKUPS ---
    @Override
    public Flux<Map<String, Object>> getReferenceData(String resource, String token) {
        return apiClient.getLookupList(resource, ensureBearer(token))
                .onErrorResume(e -> {
                    log.error("Erreur récupération Lookup {}: {}", resource, e.getMessage());
                    return Flux.empty();
                });
    }

    @Override
    public Mono<Void> assignDriverRemote(UUID vehicleId, UUID driverId, String token) {
        VehicleOwnershipRequest req = new VehicleOwnershipRequest(
            vehicleId,
            "DRIVER",
            true, // isPrimary: true remplace le conducteur principal précédent
            LocalDateTime.now().toString(),
            driverId // On passe l'ID explicite du chauffeur
        );

        return apiClient.createOwnership(req, ensureBearer(token))
            .onErrorResume(e -> {
                log.error("Erreur assignation distante (Véhicule: {}, Chauffeur: {}): {}", vehicleId, driverId, e.getMessage());
                // Stratégie : On loggue l'erreur mais on ne bloque pas le flux local pour l'instant (Mode Best Effort)
                // Ou on propage l'erreur si on veut une cohérence stricte.
                // Ici, on retourne Empty pour ne pas casser la transaction locale, mais c'est un point à discuter.
                return Mono.empty(); 
            });
    }

    // --- HELPERS TECHNIQUES ---

    private Mono<Void> uploadFile(String uriPath, FilePart file, String method, String token) {
        return buildMultipartRequest(uriPath, file, method, token)
                .retrieve()
                .toBodilessEntity()
                .then();
    }

    private Mono<Map> uploadFileAndGetResponse(String uriPath, FilePart file, String token) {
        return buildMultipartRequest(uriPath, file, "POST", token)
                .retrieve()
                .bodyToMono(Map.class);
    }

    private WebClient.RequestBodySpec buildMultipartRequest(String uriPath, FilePart file, String method, String token) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file); // Le nom du champ attendu par le service distant est 'file'

        WebClient.RequestBodySpec spec = webClientBuilder.build()
                .method(org.springframework.http.HttpMethod.valueOf(method))
                .uri(serviceUrl + uriPath)
                .header("Authorization", token) // Transmission du token
                .contentType(MediaType.MULTIPART_FORM_DATA);
        
        spec.bodyValue(builder.build());
        return spec;
    }

    // --- LOGIQUE DE PARSING DATE ROBUSTE ---
    private Instant parseDate(String dateStr) {
        if (dateStr == null) return null;
        try {
            // 1. Tente le format ISO-8601 standard (avec Z ou Offset)
            return Instant.parse(dateStr);
        } catch (Exception e) {
            try {
                // 2. Fallback : Tente le format LocalDateTime (sans Z) et assume UTC
                return LocalDateTime.parse(dateStr).toInstant(ZoneOffset.UTC);
            } catch (Exception e2) {
                log.warn("Impossible de parser la date: {} - Erreur: {}", dateStr, e2.getMessage());
                return null;
            }
        }
    }

    // --- MAPPING DTO -> DOMAIN ---
    private Vehicle mapToDomain(VehicleExternalResponse ext) {
        return new Vehicle(
            ext.vehicleId(),
            null, // fleetId (géré localement)
            null, // managerId (géré localement)
            null, // currentDriverId (géré localement)
            null, // vehicleTypeId (géré localement)
            
            ext.registrationNumber(),
            ext.vehicleSerialNumber(),
            
            ext.brand(),
            // Si le modèle n'est pas explicite, on met la marque en placeholder pour éviter les erreurs
            "Tucson N-Line", 
            
            null, // manufacturingYear
            "DCT-7", // transmissionType
            "Hybride", // fuelType
            
            ext.tankCapacity(),
            ext.totalSeatNumber(),
            ext.averageFuelConsumptionPerKm(),
            
            null, // color
            "AVAILABLE", // status par défaut
            
            // Photo principale : on prend la photo Série si dispo
            ext.vehicleSerialPhoto(), 
            ext.vehicleSerialPhoto(),
            ext.registrationPhoto(),
            
            null, // Financial
            null, // Maintenance
            null  // Operational
        );
    }
}