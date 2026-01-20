package com.cheftory.api.credit.exception;

import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.exception.ErrorMessage;

public class CreditException extends CheftoryException {
    public CreditException(ErrorMessage errorCode) {
        super(errorCode);
    }
}
