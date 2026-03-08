package com.datarain.pdp.infrastructure.security.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * اضافه شد: ریپازیتوری برای security audit log
 */
public interface SecurityAuditLogRepository extends JpaRepository<SecurityAuditLog, UUID> {

    Page<SecurityAuditLog> findByUserId(UUID userId, Pageable pageable);

    Page<SecurityAuditLog> findByEventType(SecurityEventType eventType, Pageable pageable);

    Page<SecurityAuditLog> findByEmail(String email, Pageable pageable);
}
