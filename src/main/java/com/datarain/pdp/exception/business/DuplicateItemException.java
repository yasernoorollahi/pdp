package com.datarain.pdp.exception.business;

import com.datarain.pdp.exception.base.BaseBusinessException;
import com.datarain.pdp.exception.errors.ErrorCode;
import org.springframework.http.HttpStatus;

public class DuplicateItemException extends BaseBusinessException {

    public DuplicateItemException(String title) {
        super(
                ErrorCode.DUPLICATE_ITEM,
                HttpStatus.CONFLICT,
                "Item already exists"
        );
    }
}

