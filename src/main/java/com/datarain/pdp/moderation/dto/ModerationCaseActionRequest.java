package com.datarain.pdp.moderation.dto;

import jakarta.validation.constraints.Size;

public record ModerationCaseActionRequest(
        @Size(max = 1000) String comment
) {
}
