package com.cheftory.api.user.exception;

import com.cheftory.api.exception.ErrorMessage;

public enum UserErrorCode implements ErrorMessage {

    USER_NOT_FOUND("USER_1", "회원을 찾을 수 없습니다."),
    USER_ALREADY_EXIST("USER_2", "이미 존재하는 회원입니다.");

    private final String errorCode;
    private final String message;

    UserErrorCode(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}