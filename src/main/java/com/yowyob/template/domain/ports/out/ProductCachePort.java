package com.yowyob.template.domain.ports.out;

import com.yowyob.template.domain.model.Product;
import reactor.core.publisher.Mono;

public interface ProductCachePort {
    Mono<Boolean> saveInCache(Product product);
}