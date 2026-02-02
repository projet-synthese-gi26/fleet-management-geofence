package com.yowyob.fleet.infrastructure.mappers;

import com.yowyob.fleet.domain.model.GeofenceZone;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.GeofenceZoneEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GeofenceMapper {
    
    @Mapping(target = "vertices", ignore = true)
    @Mapping(target = "activeDays", ignore = true)
    GeofenceZone toDomain(GeofenceZoneEntity entity);

    GeofenceZoneEntity toEntity(GeofenceZone domain);
}