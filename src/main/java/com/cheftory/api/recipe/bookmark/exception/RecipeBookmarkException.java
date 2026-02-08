package com.cheftory.api.recipe.bookmark.exception;

import com.cheftory.api.recipe.exception.RecipeException;

/**
 * 레시피 북마크 관련 예외
 */
public class RecipeBookmarkException extends RecipeException {

    /**
     * 에러 코드로 예외 생성
     *
     * @param errorCode 레시피 북마크 에러 코드
     */
    public RecipeBookmarkException(RecipeBookmarkErrorCode errorCode) {
        super(errorCode);
    }
}
