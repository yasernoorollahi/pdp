package com.datarain.pdp.exception.business;

import com.datarain.pdp.exception.base.BaseBusinessException;
import com.datarain.pdp.exception.errors.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends BaseBusinessException {

    public InvalidCredentialsException() {
        super(
                ErrorCode.UNAUTHORIZED,
                HttpStatus.UNAUTHORIZED,
                "Invalid credentials"
        );
    }
}
