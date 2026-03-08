package com.datarain.pdp.message.dto;

import jakarta.validation.constraints.NotNull;

public record UserMessageProcessedRequest(
        @NotNull Boolean processed
) {
}
