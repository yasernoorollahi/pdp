package com.datarain.pdp.admin.dto;

import jakarta.validation.Valid;

import java.util.List;

public record JobControlUpdateRequest(
        Boolean globalEnabled,
        @Valid List<JobToggleRequest> jobs
) {
}
