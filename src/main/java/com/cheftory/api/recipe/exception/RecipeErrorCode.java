package com.cheftory.api.recipe.exception;

import com.cheftory.api.exception.ErrorMessage;
import lombok.Getter;

@Getter
public enum RecipeErrorCode implements ErrorMessage {
    RECIPE_NOT_FOUND("RECIPE_001", "레시피가 존재하지 않습니다."),
    RECIPE_BANNED("RECIPE_002", "접근할 수 없는 레시피 입니다."),
    RECIPE_NOT_COOK_VIDEO("RECIPE_003", "요리 비디오 id가 아닙니다."),
    RECIPE_CREATE_FAIL("RECIPE_004", "레시피 생성에 실패했습니다."),
    RECIPE_FAILED("RECIPE008", "실패한 레시피 입니다."),
    RECIPE_NOT_BLOCKED_VIDEO("RECIPE009", "차단되지 않은 레시피 입니다."),
    RECIPE_QUERY_INVALID("RECIPE_010", "잘못된 레시피 조회 쿼리입니다."),
    INVALID_CUISINE_TYPE("RECIPE_011", "유효하지 않은 요리 카테고리입니다."),
    INVALID_RECOMMEND_TYPE("RECIPE_012", "유효하지 않은 추천 타입입니다.");

    private final String errorCode;
    private final String message;

    RecipeErrorCode(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }
}
