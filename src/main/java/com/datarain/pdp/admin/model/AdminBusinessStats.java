package com.datarain.pdp.admin.model;

public record AdminBusinessStats(
        long totalUsers,
        long activeUsers,
        long lockedUsers,
        long totalRefreshTokens,
        long activeRefreshTokens,
        long pendingNotifications
) {
}
