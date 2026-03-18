package com.datarain.pdp.admin.dto;

import java.time.Instant;
import java.util.Map;

public record SystemOverviewResponse(
        String overallStatus,
        Map<String, String> healthComponents,
        MetricSnapshot metrics,
        Instant generatedAt
) {
    public record MetricSnapshot(
            double jvmHeapUsedBytes,
            double jvmHeapMaxBytes,
            double jvmThreadsLive,
            double processCpuUsage,
            double systemCpuUsage,
            double processUptimeSeconds,
            double httpServerRequestsCount,
            double httpServerRequestsMeanSeconds,
            HikariSnapshot hikari
    ) {
    }

    public record HikariSnapshot(
            double activeConnections,
            double idleConnections,
            double pendingConnections,
            double maxConnections,
            double minConnections
    ) {
    }
}
