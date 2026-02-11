package com.cheftory.api.exception;

import lombok.Getter;

/**
 * 외부 서버 네트워크 예외.
 *
 * <p>외부 API 호출 시 네트워크 오류가 발생한 경우 던져집니다.</p>
 */
@Getter
public class ExternalServerNetworkException extends RuntimeException {
    /** 외부 서버 네트워크 예외 코드 */
    private final ExternalServerNetworkExceptionCode errorCode;

    /**
     * 외부 서버 네트워크 예외를 생성합니다.
     *
     * @param error 외부 서버 네트워크 예외 코드
     */
    public ExternalServerNetworkException(ExternalServerNetworkExceptionCode error) {
        this.errorCode = error;
    }
}
