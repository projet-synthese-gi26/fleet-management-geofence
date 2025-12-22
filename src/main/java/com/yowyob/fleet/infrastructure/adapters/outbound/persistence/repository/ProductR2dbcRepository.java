package com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.ProductEntity;

import java.util.UUID;

public interface ProductR2dbcRepository extends ReactiveCrudRepository<ProductEntity, UUID> {}