package com.yowyob.fleet.infrastructure.mappers;

import com.yowyob.fleet.domain.model.GeofenceZone;
import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.GeofenceZoneEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GeofenceMapper {
    
    // ✅ La cible est le Domaine, la source est l'Entité (le lien local)
    @Mapping(target = "name", constant = "Zone")
    GeofenceZone toDomain(GeofenceZoneEntity entity);

    // ✅ La cible est l'Entité, la source est le Domaine
    GeofenceZoneEntity toEntity(GeofenceZone domain);
}