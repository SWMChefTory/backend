package com.cheftory.api.recipe.content.briefing.exception;

import com.cheftory.api.exception.Error;

/**
 * 레시피 브리핑 도메인 에러 코드 열거형
 *
 * <p>레시피 브리핑 관련 작업 중 발생할 수 있는 에러 코드들을 정의합니다.</p>
 */
public enum RecipeBriefingErrorCode implements Error {
    /**
     * 브리핑 생성 실패
     */
    BRIEFING_CREATE_FAIL("RECIPE_BRIEFING_001", "레시피 브리핑 생성에 실패했습니다."),
    ;
    private final String errorCode;
    private final String message;

    /**
     * RecipeBriefingErrorCode 생성자
     *
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     */
    RecipeBriefingErrorCode(String errorCode, String message) {
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
