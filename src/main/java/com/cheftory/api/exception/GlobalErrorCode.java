package com.cheftory.api.exception;

/**
 * 전역 에러 코드.
 */
public enum GlobalErrorCode implements Error {
    /** 필수 필드가 누락됨 */
    FIELD_REQUIRED("GLOBAL_1", "필수 필드가 누락되었습니다."),
    /** 필수 헤더가 누락됨 */
    MISSING_HEADER("GLOBAL_2", "필수 헤더가 누락되었습니다."),
    /** 알 수 없는 에러 발생 */
    UNKNOWN_ERROR("GLOBAL_3", "알 수 없는 오류가 발생했습니다."),
    /** 알 수 없는 지역 */
    UNKNOWN_REGION("GLOBAL_4", "지역을 알수 없습니다.");

    private final String errorCode;
    private final String message;

    GlobalErrorCode(String errorCode, String message) {
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
