package com.cheftory.api._common.cursor;

import com.cheftory.api.exception.Error;
import com.cheftory.api.exception.ErrorType;

/**
 * 커서 관련 에러 코드
 */
public enum CursorErrorCode implements Error {
    /** 유효하지 않은 커서 */
    INVALID_CURSOR("CURSOR_001", "유효하지 않은 커서입니다.", ErrorType.VALIDATION);

    private final String code;
    private final String message;
    private final ErrorType type;

    CursorErrorCode(String code, String message, ErrorType type) {
        this.code = code;
        this.message = message;
        this.type = type;
    }

    @Override
    public String getErrorCode() {
        return code;
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
