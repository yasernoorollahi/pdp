package com.datarain.pdp.admin.service.impl;

import com.datarain.pdp.admin.dto.BusinessEventLogResponse;
import com.datarain.pdp.admin.dto.SecurityAuditLogResponse;
import com.datarain.pdp.admin.service.AdminAuditLogService;
import com.datarain.pdp.infrastructure.audit.BusinessEventLog;
import com.datarain.pdp.infrastructure.audit.BusinessEventLogRepository;
import com.datarain.pdp.infrastructure.audit.BusinessEventLogSpecification;
import com.datarain.pdp.infrastructure.audit.BusinessEventType;
import com.datarain.pdp.infrastructure.security.audit.SecurityAuditLog;
import com.datarain.pdp.infrastructure.security.audit.SecurityAuditLogRepository;
import com.datarain.pdp.infrastructure.security.audit.SecurityAuditLogSpecification;
import com.datarain.pdp.infrastructure.security.audit.SecurityEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminAuditLogServiceImpl implements AdminAuditLogService {

    private final SecurityAuditLogRepository securityAuditLogRepository;
    private final BusinessEventLogRepository businessEventLogRepository;

    @Override
    public Page<SecurityAuditLogResponse> getSecurityAuditLogs(Pageable pageable,
                                                              UUID userId,
                                                              String email,
                                                              SecurityEventType eventType) {
        return securityAuditLogRepository
                .findAll(SecurityAuditLogSpecification.filter(userId, email, eventType), pageable)
                .map(AdminAuditLogServiceImpl::toSecurityResponse);
    }

    @Override
    public Page<BusinessEventLogResponse> getBusinessEventLogs(Pageable pageable,
                                                              UUID userId,
                                                              String email,
                                                              BusinessEventType eventType) {
        return businessEventLogRepository
                .findAll(BusinessEventLogSpecification.filter(userId, email, eventType), pageable)
                .map(AdminAuditLogServiceImpl::toBusinessResponse);
    }

    private static SecurityAuditLogResponse toSecurityResponse(SecurityAuditLog entity) {
        return new SecurityAuditLogResponse(
                entity.getId(),
                entity.getUserId(),
                entity.getEmail(),
                entity.getEventType(),
                entity.getIpAddress(),
                entity.getUserAgent(),
                entity.getDetails(),
                entity.getCreatedAt(),
                entity.isSuccess()
        );
    }

    private static BusinessEventLogResponse toBusinessResponse(BusinessEventLog entity) {
        return new BusinessEventLogResponse(
                entity.getId(),
                entity.getUserId(),
                entity.getEmail(),
                entity.getEventType(),
                entity.getDetails(),
                entity.getCreatedAt(),
                entity.isSuccess()
        );
    }
}
