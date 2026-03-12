package com.datarain.pdp.exception.business;

import com.datarain.pdp.exception.base.BaseBusinessException;
import com.datarain.pdp.exception.errors.ErrorCode;
import org.springframework.http.HttpStatus;

public class UserEmailNotFoundException extends BaseBusinessException {

    public UserEmailNotFoundException(String email) {
        super(
                ErrorCode.NOT_FOUND,
                HttpStatus.NOT_FOUND,
                "User not found: " + email
        );
    }
}
