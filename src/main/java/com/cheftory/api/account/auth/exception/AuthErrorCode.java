package com.cheftory.api.account.auth.exception;

import com.cheftory.api.exception.ErrorMessage;

public enum AuthErrorCode implements ErrorMessage {

    INVALID_TOKEN("AUTH_1", "유효하지 않은 토큰입니다."),
    EXPIRED_REFRESH_TOKEN("AUTH_2", "만료된 토큰입니다."),
    INVALID_USER("AUTH_3", "유효하지 않은 사용자입니다.");

    private final String code;
    private final String message;

    AuthErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getErrorCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}