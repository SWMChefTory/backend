package com.cheftory.api.credit.exception;

import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.exception.Error;

/**
 * 크레딧 관련 도메인 예외.
 */
public class CreditException extends CheftoryException {
    public CreditException(Error errorCode) {
        super(errorCode);
    }

    public CreditException(Error errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
