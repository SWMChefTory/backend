package com.cheftory.api.recipe.content.caption.exception;

import com.cheftory.api.exception.ErrorMessage;
import lombok.Getter;

@Getter
public enum RecipeCaptionErrorCode implements ErrorMessage {
    CAPTION_NOT_FOUND("CAPTION_001", "자막이 존재하지 않습니다."),
    NOT_COOK_RECIPE("CAPTION_002", "요리 비디오 id가 아닙니다."),
    CAPTION_CREATE_FAIL("CAPTION_003", "자막 생성에 실패했습니다.");
    final String errorCode;
    final String message;

    RecipeCaptionErrorCode(String errorCode, String message) {
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
