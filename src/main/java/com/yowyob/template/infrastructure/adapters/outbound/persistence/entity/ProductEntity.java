package com.yowyob.template.infrastructure.adapters.outbound.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Table("products")
@Data @NoArgsConstructor @AllArgsConstructor
public class ProductEntity {
    @Id
    private UUID id;
    private String name;
    private BigDecimal price;
    private String status;
}