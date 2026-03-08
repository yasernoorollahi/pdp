package com.datarain.pdp.extraction.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

public record ClassifyExtractionResponse(
        @NotNull JsonNode classify
) {
}
