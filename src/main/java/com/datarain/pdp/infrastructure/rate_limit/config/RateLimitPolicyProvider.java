package com.datarain.pdp.infrastructure.rate_limit.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class RateLimitPolicyProvider {

    private final List<RateLimitPolicy> policies;

    public RateLimitPolicyProvider(RateLimitProperties properties) {
        this.policies = properties.getPolicies().stream()
                .sorted(Comparator.comparingInt((RateLimitPolicy policy) -> policy.getPathPrefix().length()).reversed())
                .toList();
    }

    public Optional<RateLimitPolicy> resolve(HttpServletRequest request) {
        String path = request.getRequestURI();

        return policies.stream()
                .filter(policy -> path.startsWith(policy.getPathPrefix()))
                .findFirst();
    }
}
