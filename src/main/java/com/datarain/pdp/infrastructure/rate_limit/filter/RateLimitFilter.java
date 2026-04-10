package com.datarain.pdp.infrastructure.rate_limit.filter;

import com.datarain.pdp.exception.business.RateLimitExceededException;
import com.datarain.pdp.exception.handler.ErrorResponseWriter;
import com.datarain.pdp.infrastructure.metrics.PdpMetrics;
import com.datarain.pdp.infrastructure.rate_limit.config.RateLimitPolicy;
import com.datarain.pdp.infrastructure.rate_limit.config.RateLimitPolicyProvider;
import com.datarain.pdp.infrastructure.rate_limit.config.RateLimitProperties;
import com.datarain.pdp.infrastructure.rate_limit.service.RateLimitDecision;
import com.datarain.pdp.infrastructure.rate_limit.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final RateLimitPolicyProvider policyProvider;
    private final RateLimitProperties properties;
    private final PdpMetrics metrics;
    private final ErrorResponseWriter errorResponseWriter;


    public RateLimitFilter(
            RateLimitService rateLimitService,
            RateLimitPolicyProvider policyProvider,
            RateLimitProperties properties,
            PdpMetrics metrics,
            ErrorResponseWriter errorResponseWriter
    ) {
        this.rateLimitService = rateLimitService;
        this.policyProvider = policyProvider;
        this.properties = properties;
        this.metrics = metrics;
        this.errorResponseWriter = errorResponseWriter;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Optional<RateLimitPolicy> policyOptional = policyProvider.resolve(request);
            if (policyOptional.isEmpty()) {
                filterChain.doFilter(request, response);
                return;
            }

            RateLimitPolicy policy = policyOptional.get();
            String key = resolveKey(request, policy);
            RateLimitDecision decision = rateLimitService.checkRateLimit(key, policy);
            writeRateLimitHeaders(response, decision);

            filterChain.doFilter(request, response);

        } catch (RateLimitExceededException ex) {
            RateLimitPolicy policy = policyProvider.resolve(request).orElse(null);
            metrics.incrementRateLimitRejected(request.getRequestURI());

            log.warn(
                    "Rate limit exceeded | key={} | method={} | path={}",
                    resolveClientIp(request),
                    request.getMethod(),
                    request.getRequestURI()
            );
            if (policy != null) {
                response.setHeader("Retry-After", String.valueOf(Math.max(1, policy.getDuration().toSeconds())));
                response.setHeader("X-RateLimit-Limit", String.valueOf(policy.getLimit()));
                response.setHeader("X-RateLimit-Remaining", "0");
                response.setHeader("X-RateLimit-Reset", String.valueOf(Math.max(1, policy.getDuration().toSeconds())));
            }

            errorResponseWriter.write(
                    response,
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Too many requests",
                    request.getRequestURI()
            );
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator")
                || path.startsWith("/swagger")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }

    private String resolveKey(HttpServletRequest request, RateLimitPolicy policy) {
        return switch (policy.getScope()) {
            case IP -> "ip:" + resolveClientIp(request);
            case USER -> "user:" + resolveAuthenticatedUser(request);
            case USER_OR_IP -> resolveUserOrIp(request);
        };
    }

    private String resolveUserOrIp(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal())) {
            return "user:" + auth.getName();
        }

        return "ip:" + resolveClientIp(request);
    }

    private String resolveAuthenticatedUser(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return resolveClientIp(request);
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (properties.isTrustForwardHeaders()) {
            String forwardedFor = request.getHeader("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isBlank()) {
                return normalizeIp(forwardedFor.split(",")[0].trim());
            }
            String realIp = request.getHeader("X-Real-IP");
            if (realIp != null && !realIp.isBlank()) {
                return normalizeIp(realIp.trim());
            }
        }
        return normalizeIp(request.getRemoteAddr());
    }

    private void writeRateLimitHeaders(HttpServletResponse response, RateLimitDecision decision) {
        response.setHeader("X-RateLimit-Limit", String.valueOf(decision.limit()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(decision.remaining()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(decision.retryAfterSeconds()));
    }

    private String normalizeIp(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            return "unknown";
        }

        String normalized = ipAddress.trim();
        int zoneSeparator = normalized.indexOf('%');
        if (zoneSeparator >= 0) {
            normalized = normalized.substring(0, zoneSeparator);
        }

        if ("::1".equals(normalized) || "0:0:0:0:0:0:0:1".equals(normalized)) {
            return "127.0.0.1";
        }

        try {
            InetAddress address = InetAddress.getByName(normalized);
            if (address.isLoopbackAddress()) {
                return "127.0.0.1";
            }
        } catch (UnknownHostException ignored) {
            return normalized;
        }

        return normalized;
    }
}
