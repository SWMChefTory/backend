package com.cheftory.api.recipe.content.verify.exception;

import com.cheftory.api.exception.Error;

public enum RecipeVerifyErrorCode implements Error {
    VIDEO_ID_NULL("VERIFY_CLIENT_001", "video id가 NULL 입니다."),
    NOT_COOK_VIDEO("VERIFY_CLIENT_002", "요리 비디오가 아닙니다."),
    SERVER_ERROR("VERIFY_CLIENT_003", "caption client와 통신하는 서버에서 오류가 발생했습니다.");
    final String errorCode;
    final String message;

    RecipeVerifyErrorCode(String errorCode, String message) {
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
