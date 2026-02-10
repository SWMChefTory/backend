package com.cheftory.api.recipe.content.step.exception;

import com.cheftory.api.exception.Error;

/**
 * 레시피 단계 도메인 에러 코드 열거형
 *
 * <p>레시피 단계 관련 작업 중 발생할 수 있는 에러 코드들을 정의합니다.</p>
 */
public enum RecipeStepErrorCode implements Error {
    /**
     * 레시피 단계 생성 실패
     */
    RECIPE_STEP_CREATE_FAIL("RECIPE_STEP_001", "레시피 단계 생성에 실패 했습니다."),
    ;
    private final String errorCode;
    private final String message;

    /**
     * RecipeStepErrorCode 생성자
     *
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     */
    RecipeStepErrorCode(String errorCode, String message) {
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
