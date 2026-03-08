package com.datarain.pdp.exception.handler;

import com.datarain.pdp.exception.base.BaseBusinessException;
import com.datarain.pdp.exception.dto.ApiErrorResponse;
import com.datarain.pdp.exception.errors.ErrorCode;
import com.datarain.pdp.exception.errors.ErrorResponse;
import com.datarain.pdp.exception.errors.FieldValidationError;
import com.datarain.pdp.exception.errors.ValidationErrorResponse;
import com.datarain.pdp.infrastructure.logging.TraceIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseBusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BaseBusinessException ex,
            HttpServletRequest request
    ) {
        ErrorResponse response = buildErrorResponse(
                ex.getStatus(),
                ex.getErrorCode().name(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(response, ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex
    ) {
        List<FieldValidationError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldValidationError(error.getField(), error.getDefaultMessage()))
                .toList();

        return ResponseEntity.badRequest().body(
                new ValidationErrorResponse(ErrorCode.VALIDATION_ERROR.name(), fieldErrors)
        );
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(
            jakarta.validation.ConstraintViolationException ex
    ) {
        List<FieldValidationError> fieldErrors = ex.getConstraintViolations()
                .stream()
                .map(v -> new FieldValidationError(v.getPropertyPath().toString(), v.getMessage()))
                .toList();

        return ResponseEntity.badRequest().body(
                new ValidationErrorResponse(ErrorCode.VALIDATION_ERROR.name(), fieldErrors)
        );
    }

    // اضافه شد: handler برای خطای دسترسی (403 Forbidden)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        log.warn("Access denied for request: {}", request.getRequestURI());
        return new ResponseEntity<>(
                buildErrorResponse(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN.name(),
                        "Access denied", request.getRequestURI()),
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
                buildErrorResponse(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.name(),
                        "Authentication required", request.getRequestURI()),
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
                buildErrorResponse(HttpStatus.CONFLICT, ErrorCode.DATA_INTEGRITY_VIOLATION.name(),
                        "Data integrity violation", request.getRequestURI()),
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
                buildErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, ErrorCode.METHOD_NOT_ALLOWED.name(),
                        "Method not allowed: " + ex.getMethod(), request.getRequestURI()),
                HttpStatus.METHOD_NOT_ALLOWED
        );
    }

    // اضافه شد: handler برای type mismatch در path variables (مثلاً UUID اشتباه)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ValidationErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex
    ) {
        FieldValidationError error = new FieldValidationError(
                ex.getName(),
                "Invalid value: " + ex.getValue()
        );
        return ResponseEntity.badRequest().body(
                new ValidationErrorResponse(ErrorCode.VALIDATION_ERROR.name(), List.of(error))
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleInvalidEnum(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();

        if (cause instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException ife) {
            Class<?> targetType = ife.getTargetType();

            if (targetType.isEnum()) {
                String fieldName = ife.getPath().isEmpty() ? "unknown" : ife.getPath().get(0).getFieldName();
                List<String> allowedValues = Arrays.stream(targetType.getEnumConstants())
                        .map(Object::toString)
                        .toList();

                return ResponseEntity.badRequest().body(
                        new ValidationErrorResponse(ErrorCode.VALIDATION_ERROR.name(),
                                List.of(new FieldValidationError(fieldName, "must be one of " + allowedValues)))
                );
            }
        }

        return ResponseEntity.badRequest().body(
                new ApiErrorResponse(ErrorCode.INVALID_REQUEST.name(), "Malformed JSON request")
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unexpected error on request {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return new ResponseEntity<>(
                buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR.name(),
                        "Unexpected error occurred", request.getRequestURI()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    private ErrorResponse buildErrorResponse(HttpStatus status, String code, String message, String path) {
        String traceId = MDC.get(TraceIdFilter.TRACE_ID);
        return new ErrorResponse(Instant.now(), status.value(), status.name(), code, message, path, traceId);
    }
}
