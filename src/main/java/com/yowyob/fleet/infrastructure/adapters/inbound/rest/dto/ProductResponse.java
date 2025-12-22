package com.yowyob.fleet.infrastructure.adapters.inbound.rest.dto;
import java.math.BigDecimal;
import java.util.UUID;


public record ProductResponse(UUID id, String name, BigDecimal price, String status) {}