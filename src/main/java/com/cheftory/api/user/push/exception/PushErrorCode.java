package com.cheftory.api.user.push.exception;

import com.cheftory.api.exception.Error;
import com.cheftory.api.exception.ErrorType;
import lombok.Getter;

@Getter
public enum PushErrorCode implements Error {
    PUSH_INVALID_PROVIDER("PUSH_001", "유효하지 않은 푸시 제공자입니다.", ErrorType.VALIDATION),
    PUSH_INVALID_PLATFORM("PUSH_002", "유효하지 않은 플랫폼입니다.", ErrorType.VALIDATION),
    PUSH_INVALID_TOKEN("PUSH_003", "유효하지 않은 푸시 토큰입니다.", ErrorType.VALIDATION);

    private final String errorCode;
    private final String message;
    private final ErrorType type;

    PushErrorCode(String errorCode, String message, ErrorType type) {
        this.errorCode = errorCode;
        this.message = message;
        this.type = type;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public ErrorType getType() {
        return type;
    }
}
