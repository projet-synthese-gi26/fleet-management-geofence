package com.yowyob.fleet.infrastructure.mappers;

import org.mapstruct.Mapper;

import com.yowyob.fleet.domain.model.Product;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.ProductRequest;
import com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto.ProductResponse;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.ProductEntity;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toDomain(ProductRequest request);
    ProductResponse toResponse(Product domain);
    
    ProductEntity toEntity(Product domain);
    Product toDomain(ProductEntity entity);
}