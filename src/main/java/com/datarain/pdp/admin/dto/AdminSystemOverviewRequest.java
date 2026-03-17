package com.datarain.pdp.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record AdminSystemOverviewRequest(
        @Min(1) @Max(200) Integer jobsLimit
) {
    public int resolveJobsLimit(int defaultLimit) {
        return jobsLimit != null ? jobsLimit : defaultLimit;
    }
}
