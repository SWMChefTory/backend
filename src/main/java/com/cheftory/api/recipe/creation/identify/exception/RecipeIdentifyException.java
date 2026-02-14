package com.cheftory.api.recipe.creation.identify.exception;

import com.cheftory.api.recipe.exception.RecipeException;

/**
 * 레시피 식별 도메인 전용 예외
 *
 * <p>레시피 생성 식별 관련 작업 중 발생하는 예외를 처리합니다.</p>
 */
public class RecipeIdentifyException extends RecipeException {

    /**
     * RecipeIdentifyException 생성자
     *
     * @param errorCode 레시피 식별 에러 코드
     */
    public RecipeIdentifyException(RecipeIdentifyErrorCode errorCode) {
        super(errorCode);
    }

    public RecipeIdentifyException(RecipeIdentifyErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
