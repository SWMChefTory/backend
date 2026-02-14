package com.cheftory.api.recipe.category.exception;

import com.cheftory.api.exception.Error;
import com.cheftory.api.exception.ErrorType;

/**
 * 레시피 카테고리 도메인 에러 코드 열거형
 *
 * <p>레시피 카테고리 관련 작업 중 발생할 수 있는 에러 코드들을 정의합니다.</p>
 */
public enum RecipeCategoryErrorCode implements Error {
    /**
     * 카테고리를 찾을 수 없음
     */
    RECIPE_CATEGORY_NOT_FOUND("RECIPE_CATEGORY_001", "해당 카테고리가 존재하지 않습니다.", ErrorType.NOT_FOUND),
    /**
     * 카테고리 이름이 비어있음
     */
    RECIPE_CATEGORY_NAME_EMPTY("RECIPE_CATEGORY_002", "카테고리 이름은 필수입니다.", ErrorType.VALIDATION);

    private final String errorCode;
    private final String message;
    private final ErrorType type;

    /**
     * RecipeCategoryErrorCode 생성자
     *
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     */
    RecipeCategoryErrorCode(String errorCode, String message, ErrorType type) {
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
