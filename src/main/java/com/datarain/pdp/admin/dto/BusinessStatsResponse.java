package com.datarain.pdp.admin.dto;

public record BusinessStatsResponse(
        long totalUsers,
        long activeUsers,
        long lockedUsers,
        long totalItems,
        long activeItems,
        long archivedItems,
        long totalRefreshTokens,
        long activeRefreshTokens,
        long pendingNotifications
) {
}
