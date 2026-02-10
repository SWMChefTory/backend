package com.cheftory.api.affiliate.coupang.exception;

import com.cheftory.api.exception.Error;

/**
 * 쿠팡 파트너스 API 관련 에러 코드 열거형.
 *
 * <p>쿠팡 API 요청 실패 시 발생할 수 있는 에러 코드를 정의합니다.</p>
 */
public enum CoupangErrorCode implements Error {
    COUPANG_API_REQUEST_FAIL("COUPANG_001", "쿠팡 API 요청에 실패했습니다.");

    final String errorCode;
    final String message;

    /**
     * 쿠팡 에러 코드를 생성합니다.
     *
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     */
    CoupangErrorCode(String errorCode, String message) {
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
