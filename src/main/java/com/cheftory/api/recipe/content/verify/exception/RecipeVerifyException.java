package com.cheftory.api.recipe.content.verify.exception;

import com.cheftory.api.recipe.exception.RecipeException;

/**
 * 레시피 검증 예외
 */
public class RecipeVerifyException extends RecipeException {

    /**
     * 레시피 검증 예외 생성
     *
     * @param e 에러 코드
     */
    public RecipeVerifyException(RecipeVerifyErrorCode e) {
        super(e);
    }

    public RecipeVerifyException(RecipeVerifyErrorCode e, Throwable cause) {
        super(e, cause);
    }
}
