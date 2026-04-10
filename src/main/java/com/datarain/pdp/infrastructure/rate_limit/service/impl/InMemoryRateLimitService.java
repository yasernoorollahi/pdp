package com.datarain.pdp.infrastructure.rate_limit.service.impl;

import com.datarain.pdp.exception.business.RateLimitExceededException;
import com.datarain.pdp.infrastructure.rate_limit.config.RateLimitPolicy;
import com.datarain.pdp.infrastructure.rate_limit.service.RateLimitDecision;
import com.datarain.pdp.infrastructure.rate_limit.service.RateLimitService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service("inMemoryRateLimitService")
public class InMemoryRateLimitService implements RateLimitService {

    private final Map<String, RequestCounter> storage = new ConcurrentHashMap<>();

    @Override
    public RateLimitDecision checkRateLimit(String key, RateLimitPolicy policy) {
        long now = System.currentTimeMillis();
        String storageKey = policy.getId() + ":" + key;

        RequestCounter counter = storage.computeIfAbsent(
                storageKey,
                k -> new RequestCounter(0, now)
        );

        synchronized (counter) {
            long windowMs = policy.getDuration().toMillis();

            if (now - counter.windowStart >= windowMs) {
                counter.reset(now);
            }

            if (counter.count >= policy.getLimit()) {
                throw new RateLimitExceededException();
            }

            counter.count++;
            long remaining = Math.max(0, policy.getLimit() - counter.count);
            long retryAfterSeconds = Math.max(1, (windowMs - (now - counter.windowStart) + 999) / 1000);
            return new RateLimitDecision(policy.getLimit(), remaining, retryAfterSeconds);
        }
    }

    private static class RequestCounter {
        int count;
        long windowStart;

        RequestCounter(int count, long windowStart) {
            this.count = count;
            this.windowStart = windowStart;
        }

        void reset(long now) {
            this.count = 0;
            this.windowStart = now;
        }
    }
}
