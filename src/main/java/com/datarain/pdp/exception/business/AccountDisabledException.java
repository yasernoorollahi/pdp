package com.datarain.pdp.exception.business;

import com.datarain.pdp.exception.base.BaseBusinessException;
import com.datarain.pdp.exception.errors.ErrorCode;
import org.springframework.http.HttpStatus;

public class AccountDisabledException extends BaseBusinessException {

    public AccountDisabledException() {
        super(
                ErrorCode.FORBIDDEN,
                HttpStatus.FORBIDDEN,
                "Account is disabled"
        );
    }
}
