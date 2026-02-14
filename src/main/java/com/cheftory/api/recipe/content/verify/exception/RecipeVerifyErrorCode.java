package com.cheftory.api.recipe.content.verify.exception;

import com.cheftory.api.exception.Error;
import com.cheftory.api.exception.ErrorType;

/**
 * 레시피 검증 관련 에러 코드
 */
public enum RecipeVerifyErrorCode implements Error {
    /**
     * 비디오 ID가 NULL인 경우
     */
    VIDEO_ID_NULL("VERIFY_CLIENT_001", "video id가 NULL 입니다.", ErrorType.VALIDATION),
    /**
     * 요리 관련 비디오가 아닌 경우
     */
    NOT_COOK_VIDEO("VERIFY_CLIENT_002", "요리 비디오가 아닙니다.", ErrorType.VALIDATION),
    /**
     * 검증 서버 통신 오류
     */
    SERVER_ERROR("VERIFY_CLIENT_003", "caption client와 통신하는 서버에서 오류가 발생했습니다.", ErrorType.INTERNAL);
    final String errorCode;
    final String message;
    final ErrorType type;

    RecipeVerifyErrorCode(String errorCode, String message, ErrorType type) {
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
