package com.cheftory.api.recipe.search.exception;

import com.cheftory.api.exception.Error;
import lombok.Getter;

@Getter
public enum RecipeSearchErrorCode implements Error {
    RECIPE_SEARCH_FAILED("RECIPE_SEARCH_001", "레시피 검색에 실패했습니다.");

    private final String errorCode;
    private final String message;

    RecipeSearchErrorCode(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }
}
