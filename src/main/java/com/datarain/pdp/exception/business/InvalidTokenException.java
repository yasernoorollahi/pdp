package com.datarain.pdp.exception.business;

import com.datarain.pdp.exception.base.BaseBusinessException;
import com.datarain.pdp.exception.errors.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidTokenException extends BaseBusinessException {

    public InvalidTokenException() {
        super(
                ErrorCode.INVALID_TOKEN,
                HttpStatus.UNAUTHORIZED,
                "Invalid refresh token"
        );
    }
}
