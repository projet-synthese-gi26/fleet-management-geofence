package com.yowyob.fleet.infrastructure.adapters.outbound.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.yowyob.fleet.domain.model.Product;
import com.yowyob.fleet.domain.ports.out.ProductRepositoryPort;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository.ProductR2dbcRepository;
import com.yowyob.fleet.infrastructure.mappers.ProductMapper;

import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class PostgresR2dbcAdapter implements ProductRepositoryPort {

    private final ProductR2dbcRepository repository;
    private final ProductMapper mapper;

    @Override
    public Mono<Product> save(Product product) {
        return repository.save(mapper.toEntity(product))
                .map(mapper::toDomain);
    }
}