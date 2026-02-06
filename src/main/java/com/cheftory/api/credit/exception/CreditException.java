package com.cheftory.api.credit.exception;

import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.exception.Error;

public class CreditException extends CheftoryException {
    public CreditException(Error errorCode) {
        super(errorCode);
    }
}
