package com.cheftory.api.account.user.exception;

import com.cheftory.api.exception.ErrorMessage;

public enum UserErrorCode implements ErrorMessage {

    USER_NOT_FOUND("USER_001", "회원을 찾을 수 없습니다."),
    USER_ALREADY_EXIST("USER_002", "이미 존재하는 회원입니다."),
    TERMS_OF_USE_NOT_AGREED("USER_003", "이용약관에 동의하지 않았습니다."),
    PRIVACY_POLICY_NOT_AGREED("USER_004", "개인정보처리방침에 동의하지 않았습니다.");

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