package com.datarain.pdp.signal.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

public record MessageSignalResponse(
        UUID id,
        UUID messageId,
        UUID userId,
        Integer signalVersion,
        JsonNode signals,
        String extractorModel,
        Integer extractionLatencyMs,
        String pipelineVersion,
        Instant createdAt,
        Instant updatedAt
) {
}
