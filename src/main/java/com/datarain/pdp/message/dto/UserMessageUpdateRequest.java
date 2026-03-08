package com.datarain.pdp.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UserMessageUpdateRequest(
        @NotBlank @Size(max = 20000) String content,
        @NotNull LocalDate messageDate
) {
}
