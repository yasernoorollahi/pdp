package com.datarain.pdp.moderation.dto;

import com.datarain.pdp.moderation.entity.ModerationReasonCategory;
import com.datarain.pdp.moderation.entity.ModerationSource;
import com.datarain.pdp.moderation.entity.ModerationTargetType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ModerationCaseCreateRequest(
        @NotNull ModerationTargetType targetType,
        @NotNull UUID targetId,
        @Min(0) @Max(100) int riskScore,
        @DecimalMin("0.0") @DecimalMax("1.0") Double aiConfidence,
        @NotNull ModerationReasonCategory reasonCategory,
        @NotNull ModerationSource source,
        @Size(max = 1000) String comment
) {
}
