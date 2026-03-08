package com.datarain.pdp.exception.business;

import com.datarain.pdp.exception.base.BaseBusinessException;
import com.datarain.pdp.exception.errors.ErrorCode;
import org.springframework.http.HttpStatus;

public class TokenRevokedException extends BaseBusinessException {

    public TokenRevokedException() {
        super(
                ErrorCode.INVALID_TOKEN,
                HttpStatus.UNAUTHORIZED,
                "Refresh token has been revoked"
        );
    }
}
