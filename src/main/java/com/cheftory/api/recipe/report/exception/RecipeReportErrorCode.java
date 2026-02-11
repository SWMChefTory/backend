package com.cheftory.api.recipe.report.exception;

import com.cheftory.api.exception.Error;

/**
 * 레시피 신고 관련 에러 코드
 */
public enum RecipeReportErrorCode implements Error {
    DUPLICATE_REPORT("REPORT_001", "이미 신고한 레시피입니다");

    private final String errorCode;
    private final String message;

    RecipeReportErrorCode(String errorCode, String message) {
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
