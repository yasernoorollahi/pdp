package com.datarain.pdp.exception.handler;

import com.datarain.pdp.exception.errors.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ErrorResponseWriter {

    private final ObjectMapper objectMapper;
    private final ErrorResponseFactory factory;

    public ErrorResponseWriter(ObjectMapper objectMapper, ErrorResponseFactory factory) {
        this.objectMapper = objectMapper;
        this.factory = factory;
    }

    public void write(HttpServletResponse response, HttpStatus status, String message, String path) throws IOException {
        ErrorResponse payload = factory.build(status, message, path);
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), payload);
    }
}
