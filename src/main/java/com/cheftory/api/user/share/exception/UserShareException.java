package com.cheftory.api.user.share.exception;

import com.cheftory.api.exception.CheftoryException;

public class UserShareException extends CheftoryException {

    public UserShareException(UserShareErrorCode errorCode) {
        super(errorCode);
    }
}
