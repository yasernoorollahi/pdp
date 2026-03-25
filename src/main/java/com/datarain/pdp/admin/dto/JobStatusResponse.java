package com.datarain.pdp.admin.dto;

public record JobStatusResponse(
        String jobKey,
        String description,
        boolean configuredEnabled,
        Boolean overrideEnabled,
        boolean effectiveEnabled
) {
}
