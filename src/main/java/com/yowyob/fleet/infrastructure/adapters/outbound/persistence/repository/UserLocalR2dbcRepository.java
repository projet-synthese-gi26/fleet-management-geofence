package com.yowyob.fleet.infrastructure.adapters.outbound.persistence.repository;

import com.yowyob.fleet.infrastructure.adapters.outbound.persistence.entity.UserLocalEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserLocalR2dbcRepository extends ReactiveCrudRepository<UserLocalEntity, UUID> {
}