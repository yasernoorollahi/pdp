package com.datarain.pdp.admin.dto;

import java.time.Instant;
import java.util.List;

public record JobControlResponse(
        boolean globalEnabled,
        Boolean globalOverride,
        List<JobStatusResponse> jobs,
        Instant generatedAt
) {
}
