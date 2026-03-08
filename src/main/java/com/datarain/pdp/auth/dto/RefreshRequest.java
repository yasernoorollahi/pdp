package com.datarain.pdp.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "refreshToken must not be blank")
        String refreshToken
) {
}
