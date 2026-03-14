package com.datarain.pdp.message.dto;

import com.datarain.pdp.message.entity.MessageProcessingStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record UserMessageResponse(
        UUID id,
        UUID userId,
        String content,
        LocalDate messageDate,
        boolean processed,
        String signalDecision,
        String processingStatus,
        Instant createdAt,
        Instant updatedAt
) {
}
