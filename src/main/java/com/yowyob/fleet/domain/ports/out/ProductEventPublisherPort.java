package com.yowyob.fleet.domain.ports.out;

import com.yowyob.fleet.domain.model.Product;

import reactor.core.publisher.Mono;

public interface ProductEventPublisherPort {
    Mono<Void> publishProductCreated(Product product);
}