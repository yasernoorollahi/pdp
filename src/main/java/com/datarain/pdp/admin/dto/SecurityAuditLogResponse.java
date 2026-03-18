package com.datarain.pdp.admin.dto;

import com.datarain.pdp.infrastructure.security.audit.SecurityEventType;

import java.time.Instant;
import java.util.UUID;

public record SecurityAuditLogResponse(
        UUID id,
        UUID userId,
        String email,
        SecurityEventType eventType,
        String ipAddress,
        String userAgent,
        String details,
        Instant createdAt,
        boolean success
) {
}
