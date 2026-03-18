package com.datarain.pdp.admin.mapper;

import com.datarain.pdp.admin.dto.BusinessStatsResponse;
import com.datarain.pdp.admin.dto.JobExecutionLogResponse;
import com.datarain.pdp.admin.model.AdminBusinessStats;
import com.datarain.pdp.infrastructure.job.monitoring.JobExecutionLog;

public final class AdminMonitoringMapper {

    private AdminMonitoringMapper() {
    }

    public static BusinessStatsResponse toBusinessStatsResponse(AdminBusinessStats stats) {
        return new BusinessStatsResponse(
                stats.totalUsers(),
                stats.activeUsers(),
                stats.lockedUsers(),
                stats.totalRefreshTokens(),
                stats.activeRefreshTokens(),
                stats.pendingNotifications()
        );
    }

    public static JobExecutionLogResponse toJobExecutionLogResponse(JobExecutionLog entity) {
        return new JobExecutionLogResponse(
                entity.getId(),
                entity.getJobName(),
                entity.getStartedAt(),
                entity.getFinishedAt(),
                entity.getStatus(),
                entity.getDuration(),
                entity.getProcessedCount(),
                entity.getErrorMessage()
        );
    }
}
