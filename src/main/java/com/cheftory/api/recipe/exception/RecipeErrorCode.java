package com.cheftory.api.recipe.exception;

import com.cheftory.api.exception.Error;
import com.cheftory.api.exception.ErrorType;
import lombok.Getter;

@Getter
public enum RecipeErrorCode implements Error {
    RECIPE_NOT_FOUND("RECIPE_001", "레시피가 존재하지 않습니다.", ErrorType.NOT_FOUND),
    RECIPE_BANNED("RECIPE_002", "접근할 수 없는 레시피 입니다.", ErrorType.VALIDATION),
    RECIPE_NOT_COOK_VIDEO("RECIPE_003", "요리 비디오 id가 아닙니다.", ErrorType.VALIDATION),
    RECIPE_CREATE_FAIL("RECIPE_004", "레시피 생성에 실패했습니다.", ErrorType.INTERNAL),
    RECIPE_FAILED("RECIPE_008", "실패한 레시피 입니다.", ErrorType.INTERNAL),
    RECIPE_NOT_BLOCKED_VIDEO("RECIPE_009", "차단되지 않은 레시피 입니다.", ErrorType.VALIDATION),
    RECIPE_QUERY_INVALID("RECIPE_010", "잘못된 레시피 조회 쿼리입니다.", ErrorType.VALIDATION),
    INVALID_CUISINE_TYPE("RECIPE_011", "유효하지 않은 요리 카테고리입니다.", ErrorType.VALIDATION),
    INVALID_RECOMMEND_TYPE("RECIPE_012", "유효하지 않은 추천 타입입니다.", ErrorType.VALIDATION);

    private final String errorCode;
    private final String message;
    private final ErrorType type;

    RecipeErrorCode(String errorCode, String message, ErrorType type) {
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
