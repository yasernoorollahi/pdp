package com.datarain.pdp.infrastructure.rate_limit.config;

import java.time.Duration;

public class RateLimitConfig {

    private final int limit;
    private final Duration duration;

    public RateLimitConfig(int limit, Duration duration) {
        this.limit = limit;
        this.duration = duration;
    }

    public int getLimit() {
        return limit;
    }

    public Duration getDuration() {
        return duration;
    }
}

