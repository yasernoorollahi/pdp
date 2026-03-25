package com.datarain.pdp.infrastructure.rate_limit.filter;

import com.datarain.pdp.exception.business.RateLimitExceededException;
import com.datarain.pdp.exception.handler.ErrorResponseWriter;
import com.datarain.pdp.infrastructure.metrics.PdpMetrics;
import com.datarain.pdp.infrastructure.rate_limit.config.RateLimitConfig;
import com.datarain.pdp.infrastructure.rate_limit.config.RateLimitPolicyProvider;
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

@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final RateLimitPolicyProvider policyProvider;
    private final PdpMetrics metrics;
    private final ErrorResponseWriter errorResponseWriter;


    public RateLimitFilter(
            RateLimitService rateLimitService,
            RateLimitPolicyProvider policyProvider,
            PdpMetrics metrics,
            ErrorResponseWriter errorResponseWriter
    ) {
        this.rateLimitService = rateLimitService;
        this.policyProvider = policyProvider;
        this.metrics = metrics;
        this.errorResponseWriter = errorResponseWriter;
    }



//version 2
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            RateLimitConfig config = policyProvider.resolve(request);
            String key = resolveKey(request);

            rateLimitService.checkRateLimit(key, config);

            filterChain.doFilter(request, response);

        } catch (RateLimitExceededException ex) {

            // لاگ تمیز، یک خط
            log.warn(
                    "Rate limit exceeded | key={} | method={} | path={}",
                    resolveKey(request),
                    request.getMethod(),
                    request.getRequestURI()
            );
            metrics.getRateLimitHitCounter().increment();

            errorResponseWriter.write(
                    response,
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Too many requests",
                    request.getRequestURI()
            );
        }
    }


    private String resolveKey(HttpServletRequest request) {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal())) {

            return auth.getName();
        }

        return request.getRemoteAddr();
    }
}
