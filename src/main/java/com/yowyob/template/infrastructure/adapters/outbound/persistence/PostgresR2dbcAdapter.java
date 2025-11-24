package com.yowyob.template.infrastructure.adapters.outbound.persistence;

import com.yowyob.template.domain.model.Product;
import com.yowyob.template.domain.ports.out.ProductRepositoryPort;
import com.yowyob.template.infrastructure.adapters.outbound.persistence.repository.ProductR2dbcRepository;
import com.yowyob.template.infrastructure.mappers.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
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