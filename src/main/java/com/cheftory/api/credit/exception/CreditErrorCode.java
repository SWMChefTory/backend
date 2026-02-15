package com.cheftory.api.credit.exception;

import com.cheftory.api.exception.Error;
import com.cheftory.api.exception.ErrorType;
import lombok.Getter;

/**
 * 크레딧 관련 에러 코드.
 */
@Getter
public enum CreditErrorCode implements Error {
    /** 크레딧 부족 */
    CREDIT_INSUFFICIENT("CREDIT_001", "크레딧이 부족합니다.", ErrorType.VALIDATION),
    /** 유효하지 않은 사용자 */
    CREDIT_INVALID_USER("CREDIT_002", "유효하지 않은 사용자입니다.", ErrorType.VALIDATION),
    /** 크레딧 동시성 충돌 */
    CREDIT_CONCURRENCY_CONFLICT("CREDIT_003", "크레딧 동시성 이유가 발생했습니다.", ErrorType.CONFLICT);
    private final String errorCode;
    private final String message;
    private final ErrorType type;

    CreditErrorCode(String errorCode, String message, ErrorType type) {
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
