package com.datarain.pdp.exception.business;

import com.datarain.pdp.exception.base.BaseBusinessException;
import com.datarain.pdp.exception.errors.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class UserNotFoundException extends BaseBusinessException {

    public UserNotFoundException(UUID id) {
        super(
                ErrorCode.NOT_FOUND,
                HttpStatus.NOT_FOUND,
                "User not found: " + id
        );
    }
}
