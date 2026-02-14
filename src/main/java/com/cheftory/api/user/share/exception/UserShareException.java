package com.cheftory.api.user.share.exception;

import com.cheftory.api.exception.CheftoryException;

/**
 * 유저 공유 도메인 전용 예외
 *
 * <p>유저 공유 관련 작업 중 발생하는 예외를 처리합니다.</p>
 */
public class UserShareException extends CheftoryException {

    /**
     * UserShareException 생성자
     *
     * @param errorCode 유저 공유 에러 코드
     */
    public UserShareException(UserShareErrorCode errorCode) {
        super(errorCode);
    }

    public UserShareException(UserShareErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
