package com.datarain.pdp.moderation.exception;

import com.datarain.pdp.exception.base.BaseBusinessException;
import com.datarain.pdp.exception.errors.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class InvalidModerationTransitionException extends BaseBusinessException {

    public InvalidModerationTransitionException(UUID id, String action, String currentStatus) {
        super(
                ErrorCode.OPERATION_NOT_ALLOWED,
                HttpStatus.CONFLICT,
                "Cannot " + action + " moderation case " + id + " from status " + currentStatus
        );
    }
}
