package com.datarain.pdp.infrastructure.logging;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID = "traceId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // اگر کلاینت خودش traceId فرستاده بود (برای آینده microservice)
        String traceId = request.getHeader("X-Trace-Id");

        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }

        // بذار تو MDC
        MDC.put(TRACE_ID, traceId);

        try {
            // می‌تونیم تو response header هم بذاریم
            response.setHeader("X-Trace-Id", traceId);

            filterChain.doFilter(request, response);
        } finally {
            // خیلی مهم: پاک کردن بعد از اتمام request
            MDC.remove(TRACE_ID);
        }
    }
}

