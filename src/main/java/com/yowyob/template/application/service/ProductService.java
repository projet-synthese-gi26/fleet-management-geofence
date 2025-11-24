package com.yowyob.template.application.service;

import com.yowyob.template.domain.exception.StockFullException;
import com.yowyob.template.domain.model.Product;
import com.yowyob.template.domain.ports.in.CreateProductUseCase;
import com.yowyob.template.domain.ports.out.ProductCachePort;
import com.yowyob.template.domain.ports.out.ProductEventPublisherPort;
import com.yowyob.template.domain.ports.out.ProductRepositoryPort;
import com.yowyob.template.domain.ports.out.StockClientPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

// import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService implements CreateProductUseCase {

    private final ProductRepositoryPort repository;
    private final StockClientPort stockClient;
    private final ProductCachePort cache;
    private final ProductEventPublisherPort publisher;

    @Override
    public Mono<Product> createProduct(Product product) {
        return stockClient.isStockFull(product.name())
                .flatMap(isFull -> {
                    if (isFull) {
                        return Mono.error(new StockFullException("Stock is full for the product : " + product.name()));
                    }
                    return Mono.just(product);
                })
                .map(p -> new Product(null, p.name(), p.price(), "CREATED"))
                .flatMap(repository::save)
                .flatMap(savedProduct -> 
                    // We cache and publish asynchronously, then return the object
                    Mono.when(
                        cache.saveInCache(savedProduct),
                        publisher.publishProductCreated(savedProduct)
                    ).thenReturn(savedProduct)
                )
                .doOnSuccess(p -> log.info("Product successfully created : {}", p.id()));
    }
}