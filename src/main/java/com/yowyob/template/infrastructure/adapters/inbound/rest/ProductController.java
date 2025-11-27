package com.yowyob.template.infrastructure.adapters.inbound.rest;

import com.yowyob.template.domain.ports.in.CreateProductUseCase;
import com.yowyob.template.infrastructure.adapters.inbound.rest.dto.ProductRequest;
import com.yowyob.template.infrastructure.adapters.inbound.rest.dto.ProductResponse;
import com.yowyob.template.infrastructure.mappers.ProductMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final CreateProductUseCase useCase;
    private final ProductMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un produit", description = "Vérifie le stock distant et sauvegarde le produit.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Produit créé avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public Mono<ProductResponse> create(@RequestBody @Valid Mono<ProductRequest> requestMono) {
        return requestMono
                .map(mapper::toDomain)
                .flatMap(useCase::createProduct)
                .map(mapper::toResponse);
    }
}