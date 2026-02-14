package com.cheftory.api.recipe.content.briefing.exception;

import com.cheftory.api.recipe.exception.RecipeException;

/**
 * 레시피 브리핑 도메인 전용 예외
 *
 * <p>레시피 브리핑 관련 작업 중 발생하는 예외를 처리합니다.</p>
 */
public class RecipeBriefingException extends RecipeException {

    /**
     * RecipeBriefingException 생성자
     *
     * @param errorCode 레시피 브리핑 에러 코드
     */
    public RecipeBriefingException(RecipeBriefingErrorCode errorCode) {
        super(errorCode);
    }

    public RecipeBriefingException(RecipeBriefingErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
