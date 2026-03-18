package com.datarain.pdp.infrastructure.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface BusinessEventLogRepository extends JpaRepository<BusinessEventLog, UUID>,
        JpaSpecificationExecutor<BusinessEventLog> {
    Page<BusinessEventLog> findByEventType(BusinessEventType eventType, Pageable pageable);
}
