package com.datarain.pdp.infrastructure.rate_limit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "pdp.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;
    private boolean redisEnabled = true;
    private boolean trustForwardHeaders = false;
    private String keyPrefix = "pdp:rate-limit";
    private List<RateLimitPolicy> policies = new ArrayList<>(defaultPolicies());

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isRedisEnabled() {
        return redisEnabled;
    }

    public void setRedisEnabled(boolean redisEnabled) {
        this.redisEnabled = redisEnabled;
    }

    public boolean isTrustForwardHeaders() {
        return trustForwardHeaders;
    }

    public void setTrustForwardHeaders(boolean trustForwardHeaders) {
        this.trustForwardHeaders = trustForwardHeaders;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public List<RateLimitPolicy> getPolicies() {
        return policies;
    }

    public void setPolicies(List<RateLimitPolicy> policies) {
        this.policies = policies == null || policies.isEmpty()
                ? new ArrayList<>(defaultPolicies())
                : policies;
    }

    private static List<RateLimitPolicy> defaultPolicies() {
        List<RateLimitPolicy> defaults = new ArrayList<>();
        defaults.add(policy("auth-login", "/api/auth/login", 10, Duration.ofMinutes(1), RateLimitScope.IP));
        defaults.add(policy("auth-refresh", "/api/auth/refresh", 30, Duration.ofMinutes(1), RateLimitScope.USER_OR_IP));
        defaults.add(policy("auth-default", "/api/auth", 50, Duration.ofMinutes(1), RateLimitScope.IP));
        defaults.add(policy("user-messages", "/api/user-messages", 120, Duration.ofMinutes(1), RateLimitScope.USER_OR_IP));
        defaults.add(policy("extraction", "/api/extraction", 30, Duration.ofMinutes(1), RateLimitScope.USER_OR_IP));
        defaults.add(policy("admin-jobs", "/api/admin/jobs", 30, Duration.ofMinutes(1), RateLimitScope.USER));
        defaults.add(policy("admin-default", "/api/admin", 60, Duration.ofMinutes(1), RateLimitScope.USER));
        defaults.add(policy("default", "/api", 100, Duration.ofMinutes(1), RateLimitScope.USER_OR_IP));
        return defaults;
    }

    private static RateLimitPolicy policy(
            String id,
            String pathPrefix,
            int limit,
            Duration duration,
            RateLimitScope scope
    ) {
        RateLimitPolicy policy = new RateLimitPolicy();
        policy.setId(id);
        policy.setPathPrefix(pathPrefix);
        policy.setLimit(limit);
        policy.setDuration(duration);
        policy.setScope(scope);
        return policy;
    }
}
