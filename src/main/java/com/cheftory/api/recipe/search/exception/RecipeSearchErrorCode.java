package com.cheftory.api.recipe.search.exception;

import com.cheftory.api.exception.Error;
import com.cheftory.api.exception.ErrorType;
import lombok.Getter;

@Getter
public enum RecipeSearchErrorCode implements Error {
    RECIPE_SEARCH_FAILED("RECIPE_SEARCH_001", "레시피 검색에 실패했습니다.", ErrorType.INTERNAL);

    private final String errorCode;
    private final String message;
    private final ErrorType type;

    RecipeSearchErrorCode(String errorCode, String message, ErrorType type) {
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
