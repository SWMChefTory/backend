package com.cheftory.api.exception;

import lombok.Getter;

/**
 * 외부 서버 네트워크 예외.
 *
 * <p>외부 API 호출 시 네트워크 오류가 발생한 경우 던져집니다.</p>
 */
@Getter
public class ExternalServerNetworkException extends RuntimeException {
    private final ExternalServerNetworkExceptionCode errorCode;

    public ExternalServerNetworkException(ExternalServerNetworkExceptionCode error) {
        this.errorCode = error;
    }
}
