package com.cheftory.api.auth.exception;

import com.cheftory.api.exception.CheftoryException;
import lombok.Getter;

/**
 * 인증 관련 예외
 */
@Getter
public class AuthException extends CheftoryException {

    /**
     * AuthException 생성
     *
     * @param errorCode 인증 에러 코드
     */
    public AuthException(AuthErrorCode errorCode) {
        super(errorCode);
    }
}
