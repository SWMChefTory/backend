package com.cheftory.api.recipe.content.detailMeta.exception;

import com.cheftory.api.recipe.exception.RecipeException;

/**
 * 레시피 상세 메타 정보 도메인 전용 예외
 *
 * <p>레시피 상세 메타 정보 관련 작업 중 발생하는 예외를 처리합니다.</p>
 */
public class RecipeDetailMetaException extends RecipeException {

    /**
     * RecipeDetailMetaException 생성자
     *
     * @param errorCode 레시피 상세 메타 정보 에러 코드
     */
    public RecipeDetailMetaException(RecipeDetailMetaErrorCode errorCode) {
        super(errorCode);
    }

    public RecipeDetailMetaException(RecipeDetailMetaErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
