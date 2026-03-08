package com.datarain.pdp.exception.base;


import com.datarain.pdp.exception.errors.ErrorCode;
import org.springframework.http.HttpStatus;


public abstract class BaseBusinessException extends RuntimeException {


    private final ErrorCode errorCode;
    private final HttpStatus status;


    protected BaseBusinessException(ErrorCode errorCode, HttpStatus status, String message) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }


    public ErrorCode getErrorCode() {
        return errorCode;
    }


    public HttpStatus getStatus() {
        return status;
    }
}
