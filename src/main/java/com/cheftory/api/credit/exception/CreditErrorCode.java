package com.cheftory.api.credit.exception;

import com.cheftory.api.exception.ErrorMessage;
import lombok.Getter;

@Getter
public enum CreditErrorCode implements ErrorMessage {
    CREDIT_INSUFFICIENT("CREDIT_001", "크레딧이 부족합니다."),
    CREDIT_INVALID_USER("CREDIT_002", "유효하지 않은 사용자입니다."),
    CREDIT_CONCURRENCY_CONFLICT("CREDIT_003", "크레딧 동시성 이유가 발생했습니다.");
    private final String errorCode;
    private final String message;

    CreditErrorCode(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }
}
