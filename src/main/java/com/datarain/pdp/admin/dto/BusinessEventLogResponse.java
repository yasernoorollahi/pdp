package com.datarain.pdp.admin.dto;

import com.datarain.pdp.infrastructure.audit.BusinessEventType;

import java.time.Instant;
import java.util.UUID;

public record BusinessEventLogResponse(
        UUID id,
        UUID userId,
        String email,
        BusinessEventType eventType,
        String details,
        Instant createdAt,
        boolean success
) {
}
