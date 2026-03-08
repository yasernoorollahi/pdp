package com.datarain.pdp.exception.business;

import com.datarain.pdp.exception.base.BaseBusinessException;
import com.datarain.pdp.exception.errors.ErrorCode;
import org.springframework.http.HttpStatus;

public class RateLimitExceededException extends BaseBusinessException {

    public RateLimitExceededException() {
        super(
                ErrorCode.RATE_LIMIT_EXCEEDED,
                HttpStatus.TOO_MANY_REQUESTS,
                "Too many requests"
        );
    }
}
