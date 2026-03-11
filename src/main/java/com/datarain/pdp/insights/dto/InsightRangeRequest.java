package com.datarain.pdp.insights.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record InsightRangeRequest(
        @Min(1) @Max(365) Integer days
) {
    public int resolveDays(int defaultDays) {
        return days != null ? days : defaultDays;
    }
}
