package com.cheftory.api.exception;

/**
 * 전역 에러 코드.
 */
public enum GlobalErrorCode implements Error {
    /** 필수 필드가 누락됨 */
    FIELD_REQUIRED("GLOBAL_001", "필수 필드가 누락되었습니다.", ErrorType.VALIDATION),
    /** 필수 헤더가 누락됨 */
    MISSING_HEADER("GLOBAL_002", "필수 헤더가 누락되었습니다.", ErrorType.VALIDATION),
    /** 알 수 없는 에러 발생 */
    UNKNOWN_ERROR("GLOBAL_003", "알 수 없는 오류가 발생했습니다.", ErrorType.INTERNAL),
    /** 알 수 없는 지역 */
    UNKNOWN_REGION("GLOBAL_004", "지역을 알수 없습니다.", ErrorType.VALIDATION);

    private final String errorCode;
    private final String message;
    private final ErrorType type;

    GlobalErrorCode(String errorCode, String message, ErrorType type) {
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
