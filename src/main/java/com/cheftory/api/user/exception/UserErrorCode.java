package com.cheftory.api.user.exception;

import com.cheftory.api.exception.Error;
import com.cheftory.api.exception.ErrorType;

/**
 * 유저 도메인 에러 코드 열거형
 *
 * <p>유저 관련 작업 중 발생할 수 있는 에러 코드들을 정의합니다.</p>
 */
public enum UserErrorCode implements Error {
    /**
     * 유저를 찾을 수 없음
     */
    USER_NOT_FOUND("USER_001", "회원을 찾을 수 없습니다.", ErrorType.NOT_FOUND),
    /**
     * 유저가 이미 존재함
     */
    USER_ALREADY_EXIST("USER_002", "이미 존재하는 회원입니다.", ErrorType.CONFLICT),
    /**
     * 이용약관 미동의
     */
    TERMS_OF_USE_NOT_AGREED("USER_003", "이용약관에 동의하지 않았습니다.", ErrorType.VALIDATION),
    /**
     * 개인정보 처리방침 미동의
     */
    PRIVACY_POLICY_NOT_AGREED("USER_004", "개인정보처리방침에 동의하지 않았습니다.", ErrorType.VALIDATION),
    /**
     * 튜토리얼 이미 완료됨
     */
    TUTORIAL_ALREADY_FINISHED("USER_005", "이미 튜토리얼을 완료했습니다.", ErrorType.CONFLICT);

    private final String errorCode;
    private final String message;
    private final ErrorType type;

    /**
     * UserErrorCode 생성자
     *
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     */
    UserErrorCode(String errorCode, String message, ErrorType type) {
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
