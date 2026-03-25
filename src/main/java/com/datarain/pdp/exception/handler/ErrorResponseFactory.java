package com.datarain.pdp.exception.handler;

import com.datarain.pdp.exception.errors.ErrorResponse;
import com.datarain.pdp.infrastructure.logging.TraceIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ErrorResponseFactory {

    public ErrorResponse build(HttpStatus status, String message, String path) {
        return build(status.value(), status.name(), message, path);
    }

    public ErrorResponse build(int status, String error, String message, String path) {
        String traceId = MDC.get(TraceIdFilter.TRACE_ID);
        return new ErrorResponse(Instant.now(), status, error, message, path, traceId);
    }

    public ErrorResponse build(HttpStatus status, String message, HttpServletRequest request) {
        return build(status, message, request.getRequestURI());
    }
}
