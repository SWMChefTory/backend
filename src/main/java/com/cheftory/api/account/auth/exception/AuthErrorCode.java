package com.cheftory.api.account.auth.exception;

import com.cheftory.api.exception.ErrorMessage;

public enum AuthErrorCode implements ErrorMessage {

    INVALID_TOKEN("AUTH_001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN("AUTH_002", "만료된 토큰입니다."),
    INVALID_USER("AUTH_003", "유효하지 않은 사용자입니다."),
    MISSING_TOKEN("AUTH_004", "토큰이 누락되었습니다.");


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