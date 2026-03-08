package com.datarain.pdp.admin.dto;

import java.time.Instant;
import java.util.List;

public record AdminSystemOverviewResponse(
        BusinessStatsResponse businessStats,
        SystemOverviewResponse system,
        List<JobExecutionLogResponse> recentJobs,
        Instant generatedAt
) {
}
