package com.yowyob.fleet.infrastructure.mappers;

import com.yowyob.fleet.domain.model.GeofenceZone;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.GeofenceZoneEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GeofenceMapper {
    GeofenceZoneEntity toEntity(GeofenceZone domain);
    GeofenceZone toDomain(GeofenceZoneEntity entity);
}