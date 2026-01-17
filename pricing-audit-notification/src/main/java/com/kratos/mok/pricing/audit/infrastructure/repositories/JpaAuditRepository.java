package com.kratos.mok.pricing.audit.infrastructure.repositories;

import com.kratos.mok.pricing.audit.infrastructure.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaAuditRepository extends JpaRepository<AuditLogEntity, String> {
    Optional<AuditLogEntity> findByAggregateId(String aggregateId);
}
