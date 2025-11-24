package com.yowyob.template.infrastructure.mappers;

import com.yowyob.template.domain.model.Product;
import com.yowyob.template.infrastructure.adapters.inbound.rest.dto.ProductRequest;
import com.yowyob.template.infrastructure.adapters.inbound.rest.dto.ProductResponse;
import com.yowyob.template.infrastructure.adapters.outbound.persistence.entity.ProductEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toDomain(ProductRequest request);
    ProductResponse toResponse(Product domain);
    
    ProductEntity toEntity(Product domain);
    Product toDomain(ProductEntity entity);
}