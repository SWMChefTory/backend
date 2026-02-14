package com.cheftory.api.auth.verifier.exception;

import com.cheftory.api.exception.CheftoryException;
import lombok.Getter;

/**
 * OAuth 토큰 검증 관련 예외
 */
@Getter
public class VerificationException extends CheftoryException {

    /**
     * VerificationException 생성
     *
     * @param errorCode 토큰 검증 에러 코드
     */
    public VerificationException(VerificationErrorCode errorCode) {
        super(errorCode);
    }

    public VerificationException(VerificationErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
