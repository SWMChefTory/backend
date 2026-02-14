package com.cheftory.api.recipe.content.detailMeta.exception;

import com.cheftory.api.exception.Error;
import com.cheftory.api.exception.ErrorType;

/**
 * 레시피 상세 메타 정보 도메인 에러 코드 열거형
 *
 * <p>레시피 상세 메타 정보 관련 작업 중 발생할 수 있는 에러 코드들을 정의합니다.</p>
 */
public enum RecipeDetailMetaErrorCode implements Error {
    /**
     * 상세 메타 정보를 찾을 수 없음
     */
    DETAIL_META_NOT_FOUND("RECIPE_DETAIL_META_001", "레시피 상세 조회에 실패 했습니다.", ErrorType.NOT_FOUND),
    ;
    private final String errorCode;
    private final String message;
    private final ErrorType type;

    /**
     * RecipeDetailMetaErrorCode 생성자
     *
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     */
    RecipeDetailMetaErrorCode(String errorCode, String message, ErrorType type) {
        this.errorCode = errorCode;
        this.message = message;
        this.type = type;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public ErrorType getType() {
        return type;
    }
}
