package com.datarain.pdp.exception.business;

import com.datarain.pdp.exception.base.BaseBusinessException;
import com.datarain.pdp.exception.errors.ErrorCode;
import org.springframework.http.HttpStatus;

public class AiExtractionServiceErrorException extends BaseBusinessException {

    public AiExtractionServiceErrorException(String message) {
        this(message, HttpStatus.BAD_GATEWAY);
    }

    public AiExtractionServiceErrorException(String message, HttpStatus status) {
        super(
                ErrorCode.DOWNSTREAM_SERVICE_ERROR,
                status,
                message == null || message.isBlank()
                        ? "AI extraction service returned an error"
                        : message
        );
    }
}
