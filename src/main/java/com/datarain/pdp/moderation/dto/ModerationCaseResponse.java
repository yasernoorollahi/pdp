package com.datarain.pdp.moderation.dto;

import com.datarain.pdp.moderation.entity.ModerationReasonCategory;
import com.datarain.pdp.moderation.entity.ModerationSource;
import com.datarain.pdp.moderation.entity.ModerationStatus;
import com.datarain.pdp.moderation.entity.ModerationTargetType;

import java.time.Instant;
import java.util.UUID;

public record ModerationCaseResponse(
        UUID id,
        ModerationTargetType targetType,
        UUID targetId,
        ModerationStatus status,
        int riskScore,
        Double aiConfidence,
        ModerationReasonCategory reasonCategory,
        ModerationSource source,
        UUID reviewedBy,
        Instant reviewedAt,
        String comment,
        Instant createdAt,
        Instant updatedAt
) {
}
