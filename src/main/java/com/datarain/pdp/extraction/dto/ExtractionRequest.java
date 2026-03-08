package com.datarain.pdp.extraction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExtractionRequest(
        @NotBlank @Size(max = 20000) String text,
        @Size(max = 50) String provider,
        @Size(max = 100) String model
) {
}
