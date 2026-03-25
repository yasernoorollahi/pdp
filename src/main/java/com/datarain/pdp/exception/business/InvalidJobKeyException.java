package com.datarain.pdp.exception.business;

import com.datarain.pdp.exception.base.BaseBusinessException;
import com.datarain.pdp.exception.errors.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidJobKeyException extends BaseBusinessException {
    public InvalidJobKeyException(String jobKey) {
        super(ErrorCode.INVALID_REQUEST, HttpStatus.BAD_REQUEST, "Invalid job key: " + jobKey);
    }
}
