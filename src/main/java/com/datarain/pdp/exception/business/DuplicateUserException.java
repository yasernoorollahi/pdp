package com.datarain.pdp.exception.business;

import com.datarain.pdp.exception.base.BaseBusinessException;
import com.datarain.pdp.exception.errors.ErrorCode;
import org.springframework.http.HttpStatus;

public class DuplicateUserException extends BaseBusinessException {

    public DuplicateUserException(String email) {
        super(
                ErrorCode.DUPLICATE_USER,
                HttpStatus.CONFLICT,
                "User with email %s already exists".formatted(email)
        );
    }
}

