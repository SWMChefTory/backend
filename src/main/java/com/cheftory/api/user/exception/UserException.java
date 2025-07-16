package com.cheftory.api.user.exception;

public class UserException extends RuntimeException {
    public final UserErrorCode errorCode;

    public UserException(UserErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}