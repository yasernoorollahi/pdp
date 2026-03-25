package com.datarain.pdp.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record JobToggleRequest(
        @NotBlank String jobKey,
        @NotNull Boolean enabled
) {
}
