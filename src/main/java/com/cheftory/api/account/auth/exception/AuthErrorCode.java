package com.cheftory.api.account.auth.exception;

import lombok.Getter;

@Getter
public enum AuthErrorCode {

    INVALID_TOKEN("AUTH_1", "유효하지 않은 토큰입니다."),
    EXPIRED_REFRESH_TOKEN("AUTH_2", "만료된 토큰입니다.");

    private final String code;
    private final String message;

    AuthErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}