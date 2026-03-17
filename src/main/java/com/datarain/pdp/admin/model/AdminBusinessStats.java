package com.datarain.pdp.admin.model;

public record AdminBusinessStats(
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
