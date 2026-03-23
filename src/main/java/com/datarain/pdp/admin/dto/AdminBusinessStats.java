package com.datarain.pdp.admin.dto;

public record AdminBusinessStats(
        long totalUsers,
        long activeUsers,
        long lockedUsers,
        long totalRefreshTokens,
        long activeRefreshTokens,
        long pendingNotifications
) {
}
