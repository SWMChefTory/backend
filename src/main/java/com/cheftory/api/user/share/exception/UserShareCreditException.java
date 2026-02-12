package com.cheftory.api.user.share.exception;

import com.cheftory.api.credit.exception.CreditException;
import com.cheftory.api.exception.Error;

public class UserShareCreditException extends CreditException {
    public UserShareCreditException(Error error) {
        super(error);
    }
}
