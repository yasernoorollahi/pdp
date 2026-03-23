package com.datarain.pdp.admin.controller;

import com.datarain.pdp.admin.dto.BusinessEventLogResponse;
import com.datarain.pdp.admin.dto.SecurityAuditLogResponse;
import com.datarain.pdp.admin.service.AdminAuditLogService;
import com.datarain.pdp.infrastructure.audit.BusinessEventType;
import com.datarain.pdp.infrastructure.security.audit.SecurityEventType;
import jakarta.validation.constraints.Email;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/admin/audit")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin / Audit", description = "Admin access to security and business audit logs.")
public class AdminAuditLogController {

    private final AdminAuditLogService adminAuditLogService;

    @GetMapping("/security")
    @Operation(summary = "List security audit logs with filters and pagination.")
    public Page<SecurityAuditLogResponse> getSecurityAuditLogs(
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) @Email String email,
            @RequestParam(required = false) SecurityEventType eventType
    ) {
        return adminAuditLogService.getSecurityAuditLogs(pageable, userId, email, eventType);
    }

    @GetMapping("/business")
    @Operation(summary = "List business event logs with filters and pagination.")
    public Page<BusinessEventLogResponse> getBusinessEventLogs(
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) @Email String email,
            @RequestParam(required = false) BusinessEventType eventType
    ) {
        return adminAuditLogService.getBusinessEventLogs(pageable, userId, email, eventType);
    }
}
