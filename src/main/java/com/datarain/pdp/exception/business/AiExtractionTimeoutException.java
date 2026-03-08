package com.datarain.pdp.exception.business;

import com.datarain.pdp.exception.base.BaseBusinessException;
import com.datarain.pdp.exception.errors.ErrorCode;
import org.springframework.http.HttpStatus;

public class AiExtractionTimeoutException extends BaseBusinessException {

    public AiExtractionTimeoutException() {
        super(
                ErrorCode.DOWNSTREAM_TIMEOUT,
                HttpStatus.GATEWAY_TIMEOUT,
                "AI extraction request timed out"
        );
    }
}
