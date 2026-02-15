package com.cheftory.api.user.exception;

import com.cheftory.api.credit.exception.CreditException;
import com.cheftory.api.exception.Error;

public class UserCreditException extends CreditException {
    public UserCreditException(Error error) {
        super(error);
    }

    public UserCreditException(Error error, Throwable cause) {
        super(error, cause);
    }
}
