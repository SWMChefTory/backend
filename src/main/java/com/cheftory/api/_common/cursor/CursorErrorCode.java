package com.cheftory.api._common.cursor;

import com.cheftory.api.exception.Error;

/**
 * 커서 관련 에러 코드
 */
public enum CursorErrorCode implements Error {
    INVALID_CURSOR("CURSOR_001", "유효하지 않은 커서입니다.");

    private final String code;
    private final String message;

    CursorErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getErrorCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
