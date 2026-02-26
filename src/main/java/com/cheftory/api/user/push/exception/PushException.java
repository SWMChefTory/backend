package com.cheftory.api.user.push.exception;

import com.cheftory.api.exception.CheftoryException;

public class PushException extends CheftoryException {
    public PushException(PushErrorCode errorCode) {
        super(errorCode);
    }

    public PushException(PushErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
