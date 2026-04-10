package com.datarain.pdp.infrastructure.rate_limit.service;

import com.datarain.pdp.infrastructure.rate_limit.config.RateLimitPolicy;

public interface RateLimitService {

    RateLimitDecision checkRateLimit(String key, RateLimitPolicy policy);
}
