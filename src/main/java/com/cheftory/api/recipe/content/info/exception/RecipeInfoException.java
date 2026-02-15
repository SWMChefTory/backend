package com.cheftory.api.recipe.content.info.exception;

import com.cheftory.api.recipe.exception.RecipeException;

/**
 * 레시피 기본 정보 도메인 전용 예외
 *
 * <p>레시피 기본 정보 관련 작업 중 발생하는 예외를 처리합니다.</p>
 */
public class RecipeInfoException extends RecipeException {

    /**
     * RecipeInfoException 생성자
     *
     * @param errorCode 레시피 기본 정보 에러 코드
     */
    public RecipeInfoException(RecipeInfoErrorCode errorCode) {
        super(errorCode);
    }

    public RecipeInfoException(RecipeInfoErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
