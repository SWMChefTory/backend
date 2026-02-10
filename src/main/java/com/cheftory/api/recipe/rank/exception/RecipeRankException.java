package com.cheftory.api.recipe.rank.exception;

import com.cheftory.api.recipe.exception.RecipeException;

/**
 * 레시피 랭킹 관련 예외.
 *
 * <p>레시피 랭킹 조회, 생성 등의 작업 중 오류가 발생한 경우 던져집니다.</p>
 */
public class RecipeRankException extends RecipeException {
    public RecipeRankException(RecipeRankErrorCode errorCode) {
        super(errorCode);
    }
}
