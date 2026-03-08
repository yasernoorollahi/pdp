package com.datarain.pdp.exception.business;

import com.datarain.pdp.exception.base.BaseBusinessException;
import com.datarain.pdp.exception.errors.ErrorCode;
import org.springframework.http.HttpStatus;


import java.util.UUID;

public class ItemNotFoundException extends BaseBusinessException {

    public ItemNotFoundException(UUID id) {
        super(
                ErrorCode.ITEM_NOT_FOUND,
                HttpStatus.NOT_FOUND,
                "Item not found"
        );
    }
}
