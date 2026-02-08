package com.cheftory.api.user.exception;

import com.cheftory.api.exception.CheftoryException;

/**
 * 유저 도메인 전용 예외
 *
 * <p>유저 관련 작업 중 발생하는 예외를 처리합니다.</p>
 */
public class UserException extends CheftoryException {

    /**
     * UserException 생성자
     *
     * @param errorCode 유저 에러 코드
     */
    public UserException(UserErrorCode errorCode) {
        super(errorCode);
    }
}
