package com.datarain.pdp.exception.handler;

import com.datarain.pdp.exception.base.BaseBusinessException;
import com.datarain.pdp.exception.errors.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ErrorResponseFactory errorResponseFactory;

    public GlobalExceptionHandler(ErrorResponseFactory errorResponseFactory) {
        this.errorResponseFactory = errorResponseFactory;
    }

    @ExceptionHandler(BaseBusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BaseBusinessException ex,
            HttpServletRequest request
    ) {
        ErrorResponse response = errorResponseFactory.build(
                ex.getStatus(),
                ex.getMessage(),
                request
        );
        return new ResponseEntity<>(response, ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponse response = errorResponseFactory.build(
                HttpStatus.BAD_REQUEST,
                message.isBlank() ? "Validation failed" : message,
                request
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            jakarta.validation.ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));

        ErrorResponse response = errorResponseFactory.build(
                HttpStatus.BAD_REQUEST,
                message.isBlank() ? "Validation failed" : message,
                request
        );
        return ResponseEntity.badRequest().body(response);
    }

    // اضافه شد: handler برای خطای دسترسی (403 Forbidden)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        log.warn("Access denied for request: {}", request.getRequestURI());
        return new ResponseEntity<>(
                errorResponseFactory.build(HttpStatus.FORBIDDEN, "Access denied", request),
                HttpStatus.FORBIDDEN
        );
    }

    // اضافه شد: handler برای خطای Authentication (401 Unauthorized)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        return new ResponseEntity<>(
                errorResponseFactory.build(HttpStatus.UNAUTHORIZED, "Authentication required", request),
                HttpStatus.UNAUTHORIZED
        );
    }

    // اضافه شد: handler برای DataIntegrityViolation از DB
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        log.error("Data integrity violation: {}", ex.getMessage());
        return new ResponseEntity<>(
                errorResponseFactory.build(HttpStatus.CONFLICT, "Data integrity violation", request),
                HttpStatus.CONFLICT
        );
    }

    // اضافه شد: handler برای method not supported (405)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request
    ) {
        return new ResponseEntity<>(
                errorResponseFactory.build(
                        HttpStatus.METHOD_NOT_ALLOWED,
                        "Method not allowed: " + ex.getMethod(),
                        request
                ),
                HttpStatus.METHOD_NOT_ALLOWED
        );
    }

    // اضافه شد: handler برای type mismatch در path variables (مثلاً UUID اشتباه)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String message = ex.getName() + ": Invalid value: " + ex.getValue();
        ErrorResponse response = errorResponseFactory.build(HttpStatus.BAD_REQUEST, message, request);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidEnum(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        Throwable cause = ex.getCause();

        if (cause instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException ife) {
            Class<?> targetType = ife.getTargetType();

            if (targetType.isEnum()) {
                String fieldName = ife.getPath().isEmpty() ? "unknown" : ife.getPath().get(0).getFieldName();
                List<String> allowedValues = Arrays.stream(targetType.getEnumConstants())
                        .map(Object::toString)
                        .toList();

                ErrorResponse response = errorResponseFactory.build(
                        HttpStatus.BAD_REQUEST,
                        fieldName + ": must be one of " + allowedValues,
                        request
                );
                return ResponseEntity.badRequest().body(response);
            }
        }

        ErrorResponse response = errorResponseFactory.build(
                HttpStatus.BAD_REQUEST,
                "Malformed JSON request",
                request
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unexpected error on request {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return new ResponseEntity<>(
                errorResponseFactory.build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred", request),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
