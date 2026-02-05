package com.yowyob.fleet.infrastructure.mappers;

import com.yowyob.fleet.domain.model.GeofenceZone;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.GeofenceZoneEntity;
import org.mapstruct.Builder; // ATTENTION : import org.mapstruct.Builder
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface GeofenceMapper {
    
    // Entity -> Domain
    @Mapping(target = "radius", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "minDwellTime", ignore = true)
    GeofenceZone toDomain(GeofenceZoneEntity entity);

    // Domain -> Entity
    @Mapping(target = "radius", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "minDwellTime", ignore = true)
    @Mapping(target = "new", ignore = true) // C'est la propriété de l'interface Persistable
    GeofenceZoneEntity toEntity(GeofenceZone domain);
}