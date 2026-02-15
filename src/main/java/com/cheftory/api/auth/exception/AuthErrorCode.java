package com.cheftory.api.auth.exception;

import com.cheftory.api.exception.Error;
import com.cheftory.api.exception.ErrorType;

/**
 * 인증 관련 에러 코드
 */
public enum AuthErrorCode implements Error {
    /** 유효하지 않은 토큰입니다. */
    INVALID_TOKEN("AUTH_001", "유효하지 않은 토큰입니다.", ErrorType.UNAUTHORIZED),
    /** 만료된 토큰입니다. */
    EXPIRED_TOKEN("AUTH_002", "만료된 토큰입니다.", ErrorType.UNAUTHORIZED),
    /** 유효하지 않은 사용자입니다. */
    INVALID_USER("AUTH_003", "유효하지 않은 사용자입니다.", ErrorType.VALIDATION),
    /** 토큰이 누락되었습니다. */
    MISSING_TOKEN("AUTH_004", "토큰이 누락되었습니다.", ErrorType.UNAUTHORIZED),
    /** 지원하지 않는 인증 제공자입니다. */
    UNSUPPORTED_PROVIDER("AUTH_005", "지원하지 않는 인증 제공자입니다.", ErrorType.VALIDATION),
    /** 유효하지 않은 ID 토큰입니다. */
    INVALID_ID_TOKEN("AUTH_006", "유효하지 않은 ID 토큰입니다.", ErrorType.VALIDATION),
    /** 유효하지 않은 리프레시 토큰입니다. */
    INVALID_REFRESH_TOKEN("AUTH_007", "유효하지 않은 리프레시 토큰입니다.", ErrorType.UNAUTHORIZED),
    /** 유효하지 않은 엑세스 토큰입니다. */
    INVALID_ACCESS_TOKEN("AUTH_008", "유효하지 않은 엑세스 토큰입니다.", ErrorType.UNAUTHORIZED);

    private final String code;
    private final String message;
    private final ErrorType type;

    AuthErrorCode(String code, String message, ErrorType type) {
        this.code = code;
        this.message = message;
        this.type = type;
    }

    @Override
    public String getErrorCode() {
        return code;
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
