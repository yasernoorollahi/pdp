package com.datarain.pdp.infrastructure.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BusinessEventLogRepository extends JpaRepository<BusinessEventLog, UUID> {
    Page<BusinessEventLog> findByEventType(BusinessEventType eventType, Pageable pageable);
}
