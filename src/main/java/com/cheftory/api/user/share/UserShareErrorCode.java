package com.cheftory.api.user.share;

import com.cheftory.api.exception.Error;

public enum UserShareErrorCode implements Error {
    USER_SHARE_LIMIT_EXCEEDED("USER_SHARE_001", "일일 공유 횟수를 초과했습니다"),
    USER_SHARE_CREATE_FAIL("USER_SHARE_002", "공유 생성에 실패했습니다");

    private final String errorCode;
    private final String message;

    UserShareErrorCode(String errorCode, String message) {
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
