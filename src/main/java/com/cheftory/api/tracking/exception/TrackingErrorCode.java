package com.cheftory.api.tracking.exception;

import com.cheftory.api.exception.Error;
import com.cheftory.api.exception.ErrorType;

/**
 * 레시피 추적 관련 에러 코드
 */
public enum TrackingErrorCode implements Error {
    INVALID_SURFACE_TYPE("TRACKING_001", "유효하지 않은 surface type입니다", ErrorType.VALIDATION),
    EMPTY_IMPRESSIONS("TRACKING_002", "노출 데이터가 비어있습니다", ErrorType.VALIDATION);

    private final String errorCode;
    private final String message;
    private final ErrorType type;

    TrackingErrorCode(String errorCode, String message, ErrorType type) {
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
