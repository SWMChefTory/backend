package com.cheftory.api.recipe.content.step.exception;

import com.cheftory.api.recipe.exception.RecipeException;

/**
 * 레시피 단계 도메인 전용 예외
 *
 * <p>레시피 단계 관련 작업 중 발생하는 예외를 처리합니다.</p>
 */
public class RecipeStepException extends RecipeException {

    /**
     * RecipeStepException 생성자
     *
     * @param errorCode 레시피 단계 에러 코드
     */
    public RecipeStepException(RecipeStepErrorCode errorCode) {
        super(errorCode);
    }
}
