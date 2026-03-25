package com.datarain.pdp.infrastructure.job.control;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

public enum ManagedJob {
    USER_MESSAGE_ANALYSIS("UserMessageAnalysisJob", "jobs.user-message-analysis.enabled",
            "Classification stage for pending messages"),
    AI_SIGNAL_ENGINE("AiSignalEngineJob", "jobs.ai-signal-engine.enabled",
            "Signal extraction stage for useful messages"),
    SIGNAL_NORMALIZATION("SignalNormalizationJob", "jobs.signal-normalization.enabled",
            "Normalization pipeline for extracted signals"),
    PURGE_EXPIRED_REFRESH_TOKENS("PurgeExpiredRefreshTokensJob", "jobs.refresh-token.enabled",
            "Deletes expired refresh tokens"),
    NOTIFICATION_EMAIL("NotificationEmailJob", "jobs.notification-email.enabled",
            "Marks pending notifications as sent"),
    TEST_DATA_SEEDING("TestDataSeedingJob", "jobs.test-data.enabled",
            "Seeds synthetic dataset on startup");

    private static final Map<String, ManagedJob> BY_KEY =
            Arrays.stream(values()).collect(java.util.stream.Collectors.toMap(ManagedJob::getKey, Function.identity()));

    private final String key;
    private final String propertyKey;
    private final String description;

    ManagedJob(String key, String propertyKey, String description) {
        this.key = key;
        this.propertyKey = propertyKey;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public String getDescription() {
        return description;
    }

    public static ManagedJob fromKey(String key) {
        return BY_KEY.get(key);
    }
}
