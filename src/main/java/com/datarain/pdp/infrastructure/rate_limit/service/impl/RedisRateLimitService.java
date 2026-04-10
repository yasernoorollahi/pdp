package com.datarain.pdp.infrastructure.rate_limit.service.impl;

import com.datarain.pdp.exception.business.RateLimitExceededException;
import com.datarain.pdp.infrastructure.rate_limit.config.RateLimitPolicy;
import com.datarain.pdp.infrastructure.rate_limit.config.RateLimitProperties;
import com.datarain.pdp.infrastructure.rate_limit.service.RateLimitDecision;
import com.datarain.pdp.infrastructure.rate_limit.service.RateLimitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Slf4j
@Primary
@Service
public class RedisRateLimitService implements RateLimitService {

    private static final DefaultRedisScript<List> RATE_LIMIT_SCRIPT = new DefaultRedisScript<>(
            """
            local current = redis.call('INCR', KEYS[1])
            if current == 1 then
              redis.call('PEXPIRE', KEYS[1], ARGV[2])
            end
            local ttl = redis.call('PTTL', KEYS[1])
            return { current, ttl }
            """,
            List.class
    );

    private final StringRedisTemplate redisTemplate;
    private final RateLimitService fallbackRateLimitService;
    private final RateLimitProperties properties;

    public RedisRateLimitService(
            StringRedisTemplate redisTemplate,
            @Qualifier("inMemoryRateLimitService") RateLimitService fallbackRateLimitService,
            RateLimitProperties properties
    ) {
        this.redisTemplate = redisTemplate;
        this.fallbackRateLimitService = fallbackRateLimitService;
        this.properties = properties;
    }

    @Override
    public RateLimitDecision checkRateLimit(String key, RateLimitPolicy policy) {
        if (!properties.isRedisEnabled()) {
            return fallbackRateLimitService.checkRateLimit(key, policy);
        }

        String redisKey = properties.getKeyPrefix() + ":" + policy.getId() + ":" + key;

        try {
            List results = redisTemplate.execute(
                    RATE_LIMIT_SCRIPT,
                    List.of(redisKey),
                    String.valueOf(policy.getLimit()),
                    String.valueOf(policy.getDuration().toMillis())
            );

            if (results == null || results.size() < 2) {
                return fallbackRateLimitService.checkRateLimit(key, policy);
            }

            long currentCount = ((Number) results.get(0)).longValue();
            long ttlMs = Math.max(0, ((Number) results.get(1)).longValue());
            long remaining = Math.max(0, policy.getLimit() - currentCount);
            long retryAfterSeconds = ttlMs == 0
                    ? Math.max(1, policy.getDuration().toSeconds())
                    : Math.max(1, Duration.ofMillis(ttlMs).toSeconds());

            if (currentCount > policy.getLimit()) {
                throw new RateLimitExceededException();
            }

            return new RateLimitDecision(policy.getLimit(), remaining, retryAfterSeconds);
        } catch (RedisConnectionFailureException | IllegalStateException ex) {
            log.warn("Redis rate limiting unavailable, falling back to in-memory limiter", ex);
            return fallbackRateLimitService.checkRateLimit(key, policy);
        }
    }
}
