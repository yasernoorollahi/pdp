package com.datarain.pdp.exception.business;

import com.datarain.pdp.exception.base.BaseBusinessException;
import com.datarain.pdp.exception.errors.ErrorCode;
import org.springframework.http.HttpStatus;

public class AiExtractionServiceUnavailableException extends BaseBusinessException {

    public AiExtractionServiceUnavailableException(String message) {
        super(
                ErrorCode.DOWNSTREAM_SERVICE_UNAVAILABLE,
                HttpStatus.BAD_GATEWAY,
                message == null || message.isBlank()
                        ? "AI extraction service is unavailable"
                        : message
        );
    }
}
