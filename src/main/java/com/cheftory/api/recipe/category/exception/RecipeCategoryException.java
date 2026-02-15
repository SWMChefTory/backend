package com.cheftory.api.recipe.category.exception;

import com.cheftory.api.recipe.exception.RecipeException;

/**
 * 레시피 카테고리 도메인 전용 예외
 *
 * <p>레시피 카테고리 관련 작업 중 발생하는 예외를 처리합니다.</p>
 */
public class RecipeCategoryException extends RecipeException {

    /**
     * RecipeCategoryException 생성자
     *
     * @param errorCode 레시피 카테고리 에러 코드
     */
    public RecipeCategoryException(RecipeCategoryErrorCode errorCode) {
        super(errorCode);
    }

    public RecipeCategoryException(RecipeCategoryErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
