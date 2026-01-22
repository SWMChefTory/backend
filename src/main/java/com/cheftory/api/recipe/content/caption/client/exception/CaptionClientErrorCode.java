package com.cheftory.api.recipe.content.caption.client.exception;

import com.cheftory.api.exception.ErrorMessage;

public enum CaptionClientErrorCode implements ErrorMessage {
    VIDEO_ID_NULL("CAPTION_CLIENT_001", "video id가 NULL 입니다."),
    NOT_COOK_VIDEO("CAPTION_CLIENT_002", "요리 비디오가 아닙니다."),
    SERVER_ERROR("CAPTION_CLIENT_003", "caption client와 통신하는 서버에서 오류가 발생했습니다.");
    final String errorCode;
    final String message;

    CaptionClientErrorCode(String errorCode, String message) {
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
