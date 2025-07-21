package com.cheftory.api.account.auth.exception;

import com.cheftory.api.exception.CheftoryException;
import lombok.Getter;

@Getter
public class AuthException extends CheftoryException {

    public AuthException(AuthErrorCode errorCode) {
      super(errorCode);
    }
}