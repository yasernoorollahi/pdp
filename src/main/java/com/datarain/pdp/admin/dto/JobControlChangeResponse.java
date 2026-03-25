package com.datarain.pdp.admin.dto;

import java.time.Instant;
import java.util.UUID;

public record JobControlChangeResponse(
        String jobKey,
        Boolean enabledOverride,
        Instant updatedAt,
        UUID updatedBy
) {
}
