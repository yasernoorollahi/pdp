package com.datarain.pdp.signal.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

public record CombinedSignalData(
        @NotNull JsonNode facts,
        @NotNull JsonNode intent,
        @NotNull JsonNode tone,
        @NotNull JsonNode cognitive,
        @NotNull JsonNode context,
        @NotNull JsonNode topics
) {
}
