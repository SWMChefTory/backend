package com.cheftory.api.recipe.content.scene.exception;

import com.cheftory.api.exception.Error;
import com.cheftory.api.exception.ErrorType;

/**
 * 레시피 scene 도메인 에러 코드.
 */
public enum RecipeSceneErrorCode implements Error {
    RECIPE_SCENE_CREATE_FAIL("RECIPE_SCENE_001", "레시피 scene 생성에 실패했습니다.", ErrorType.INTERNAL),
    ;

    private final String errorCode;
    private final String message;
    private final ErrorType type;

    RecipeSceneErrorCode(String errorCode, String message, ErrorType type) {
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
