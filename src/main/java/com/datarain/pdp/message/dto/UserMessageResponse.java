package com.datarain.pdp.message.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record UserMessageResponse(
        UUID id,
        UUID userId,
        String content,
        LocalDate messageDate,
        boolean processed,
        Instant createdAt,
        Instant updatedAt
) {
}
