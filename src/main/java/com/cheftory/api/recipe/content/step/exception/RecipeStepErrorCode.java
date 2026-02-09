package com.cheftory.api.recipe.content.step.exception;

import com.cheftory.api.exception.Error;

public enum RecipeStepErrorCode implements Error {
    RECIPE_STEP_CREATE_FAIL("RECIPE_STEP_001", "레시피 단계 생성에 실패 했습니다."),
    ;
    final String errorCode;
    final String message;

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
