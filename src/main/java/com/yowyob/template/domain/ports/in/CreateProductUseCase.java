package com.yowyob.template.domain.ports.in;

import com.yowyob.template.domain.model.Product;
import reactor.core.publisher.Mono;

public interface CreateProductUseCase {
    Mono<Product> createProduct(Product product);
}