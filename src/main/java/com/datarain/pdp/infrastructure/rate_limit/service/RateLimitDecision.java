package com.datarain.pdp.infrastructure.rate_limit.service;

public record RateLimitDecision(
        int limit,
        long remaining,
        long retryAfterSeconds
) {
}
