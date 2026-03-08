package com.datarain.pdp.infrastructure.rate_limit.service.impl;

import com.datarain.pdp.exception.business.RateLimitExceededException;
import com.datarain.pdp.infrastructure.rate_limit.config.RateLimitConfig;
import com.datarain.pdp.infrastructure.rate_limit.service.RateLimitService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryRateLimitService implements RateLimitService {

    private final Map<String, RequestCounter> storage = new ConcurrentHashMap<>();

    @Override
    public void checkRateLimit(String key, RateLimitConfig config) {

        long now = System.currentTimeMillis();

        RequestCounter counter = storage.computeIfAbsent(
                key,
                k -> new RequestCounter(0, now)
        );

        synchronized (counter) {
            if (now - counter.windowStart > config.getDuration().toMillis()) {
                counter.reset(now);
            }

            if (counter.count >= config.getLimit()) {
                throw new RateLimitExceededException();
            }

            counter.count++;
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

