package com.datarain.pdp.infrastructure.rate_limit.service;

import com.datarain.pdp.infrastructure.rate_limit.config.RateLimitConfig;

public interface RateLimitService {

    void checkRateLimit(String key, RateLimitConfig config);
}

