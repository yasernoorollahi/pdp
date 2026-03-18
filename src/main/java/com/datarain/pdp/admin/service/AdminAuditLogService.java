package com.datarain.pdp.admin.service;

import com.datarain.pdp.admin.dto.BusinessEventLogResponse;
import com.datarain.pdp.admin.dto.SecurityAuditLogResponse;
import com.datarain.pdp.infrastructure.audit.BusinessEventType;
import com.datarain.pdp.infrastructure.security.audit.SecurityEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminAuditLogService {

    Page<SecurityAuditLogResponse> getSecurityAuditLogs(Pageable pageable,
                                                       UUID userId,
                                                       String email,
                                                       SecurityEventType eventType);

    Page<BusinessEventLogResponse> getBusinessEventLogs(Pageable pageable,
                                                       UUID userId,
                                                       String email,
                                                       BusinessEventType eventType);
}
