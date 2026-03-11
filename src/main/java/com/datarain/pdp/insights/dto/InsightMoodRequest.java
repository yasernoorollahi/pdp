package com.datarain.pdp.insights.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record InsightMoodRequest(
        @Min(1) @Max(365) Integer days,
        @Min(1) @Max(200) Integer limit
) {
    public int resolveDays(int defaultDays) {
        return days != null ? days : defaultDays;
    }

    public int resolveLimit(int defaultLimit) {
        return limit != null ? limit : defaultLimit;
    }
}
