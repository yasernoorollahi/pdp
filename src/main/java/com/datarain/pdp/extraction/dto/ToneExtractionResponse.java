package com.datarain.pdp.extraction.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

public record ToneExtractionResponse(
        @NotNull JsonNode tone
) {
}
