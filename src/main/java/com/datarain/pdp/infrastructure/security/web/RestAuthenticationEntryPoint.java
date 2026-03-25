package com.datarain.pdp.infrastructure.security.web;

import com.datarain.pdp.exception.handler.ErrorResponseWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ErrorResponseWriter errorResponseWriter;

    public RestAuthenticationEntryPoint(ErrorResponseWriter errorResponseWriter) {
        this.errorResponseWriter = errorResponseWriter;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        errorResponseWriter.write(response, HttpStatus.UNAUTHORIZED, "Authentication required", request.getRequestURI());
    }
}
