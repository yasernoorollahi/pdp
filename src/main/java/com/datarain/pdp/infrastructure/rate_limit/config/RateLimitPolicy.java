package com.datarain.pdp.infrastructure.rate_limit.config;

import java.time.Duration;

public class RateLimitPolicy {

    private String id;
    private String pathPrefix;
    private int limit;
    private Duration duration;
    private RateLimitScope scope = RateLimitScope.USER_OR_IP;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPathPrefix() {
        return pathPrefix;
    }

    public void setPathPrefix(String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public RateLimitScope getScope() {
        return scope;
    }

    public void setScope(RateLimitScope scope) {
        this.scope = scope;
    }
}
