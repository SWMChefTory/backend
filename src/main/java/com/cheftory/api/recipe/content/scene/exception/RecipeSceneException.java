package com.cheftory.api.recipe.content.scene.exception;

import com.cheftory.api.recipe.exception.RecipeException;

/**
 * 레시피 scene 도메인 전용 예외.
 */
public class RecipeSceneException extends RecipeException {

    public RecipeSceneException(RecipeSceneErrorCode errorCode) {
        super(errorCode);
    }

    public RecipeSceneException(RecipeSceneErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
