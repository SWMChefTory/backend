package com.cheftory.api.user.share.exception;

import com.cheftory.api.exception.Error;
import com.cheftory.api.exception.ErrorType;

/**
 * 유저 공유 도메인 에러 코드 열거형
 *
 * <p>유저 공유 관련 작업 중 발생할 수 있는 에러 코드들을 정의합니다.</p>
 */
public enum UserShareErrorCode implements Error {
    /**
     * 일일 공유 횟수 초과
     */
    USER_SHARE_LIMIT_EXCEEDED("USER_SHARE_001", "일일 공유 횟수를 초과했습니다", ErrorType.VALIDATION),
    /**
     * 공유 생성 실패
     */
    USER_SHARE_CREATE_FAIL("USER_SHARE_002", "공유 생성에 실패했습니다", ErrorType.INTERNAL),
    /**
     * 공유 기록을 찾을 수 없음
     */
    USER_SHARE_NOT_FOUND("USER_SHARE_003", "일일 공유하기가 존재하지 않습니다.", ErrorType.NOT_FOUND);

    /**
     * 에러 코드
     */
    private final String errorCode;
    /**
     * 에러 메시지
     */
    private final String message;

    private final ErrorType type;

    /**
     * UserShareErrorCode 생성자
     *
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     */
    UserShareErrorCode(String errorCode, String message, ErrorType type) {
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
