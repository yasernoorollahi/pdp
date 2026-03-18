package com.datarain.pdp.infrastructure.rate_limit.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RateLimitPolicyProvider {

    public RateLimitConfig resolve(HttpServletRequest request) {

        String path = request.getRequestURI();


        //ترتیب تعریف باید اینجوری باشه : specific → general
        if (path.startsWith("/api/auth/login")) {
            return new RateLimitConfig(100, Duration.ofMinutes(1));
        }

        if (path.startsWith("/api/auth")) {
            return new RateLimitConfig(50, Duration.ofMinutes(1));
        }

        if (path.startsWith("/api/user-messages")) {
            return new RateLimitConfig(100, Duration.ofMinutes(1));
        }

        if (path.startsWith("/api/extraction")) {
            return new RateLimitConfig(50, Duration.ofMinutes(1));
        }

        return new RateLimitConfig(100, Duration.ofMinutes(1));
    }
}
