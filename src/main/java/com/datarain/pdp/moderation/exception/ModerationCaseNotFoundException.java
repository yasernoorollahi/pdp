package com.datarain.pdp.moderation.exception;

import com.datarain.pdp.exception.base.BaseBusinessException;
import com.datarain.pdp.exception.errors.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class ModerationCaseNotFoundException extends BaseBusinessException {

    public ModerationCaseNotFoundException(UUID id) {
        super(
                ErrorCode.NOT_FOUND,
                HttpStatus.NOT_FOUND,
                "Moderation case not found: " + id
        );
    }
}
