package com.cheftory.api.account.auth.exception;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {
    public final AuthErrorCode errorCode;

    public AuthException(AuthErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}