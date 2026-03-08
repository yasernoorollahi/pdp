package com.datarain.pdp.admin.dto;

import com.datarain.pdp.infrastructure.job.monitoring.JobExecutionStatus;

import java.time.Instant;
import java.util.UUID;

public record JobExecutionLogResponse(
        UUID id,
        String jobName,
        Instant startedAt,
        Instant finishedAt,
        JobExecutionStatus status,
        long duration,
        long processedCount,
        String errorMessage
) {
}
