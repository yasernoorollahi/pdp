package com.datarain.pdp.exception.business;

import com.datarain.pdp.exception.base.BaseBusinessException;
import com.datarain.pdp.exception.errors.ErrorCode;
import org.springframework.http.HttpStatus;

public class TokenExpiredException extends BaseBusinessException {

    public TokenExpiredException() {
        super(
                ErrorCode.TOKEN_EXPIRED,
                HttpStatus.UNAUTHORIZED,
                "Refresh token has expired"
        );
    }
}
