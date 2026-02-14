package com.cheftory.api.affiliate.coupang.exception;

import com.cheftory.api.exception.Error;
import com.cheftory.api.exception.ErrorType;

/**
 * 쿠팡 파트너스 API 관련 에러 코드 열거형.
 *
 * <p>쿠팡 API 요청 실패 시 발생할 수 있는 에러 코드를 정의합니다.</p>
 */
public enum CoupangErrorCode implements Error {
    /** 쿠팡 API 요청 실패 */
    COUPANG_API_REQUEST_FAIL("COUPANG_001", "쿠팡 API 요청에 실패했습니다.", ErrorType.INTERNAL);

    final String errorCode;
    final String message;
    final ErrorType type;

    /**
     * 쿠팡 에러 코드를 생성합니다.
     *
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     */
    CoupangErrorCode(String errorCode, String message, ErrorType type) {
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
