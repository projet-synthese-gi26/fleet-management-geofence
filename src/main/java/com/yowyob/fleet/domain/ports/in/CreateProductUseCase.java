package com.yowyob.fleet.domain.ports.in;

import com.yowyob.fleet.domain.model.Product;

import reactor.core.publisher.Mono;

public interface CreateProductUseCase {
    Mono<Product> createProduct(Product product);
}