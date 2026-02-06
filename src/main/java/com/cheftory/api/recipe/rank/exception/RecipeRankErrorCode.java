package com.cheftory.api.recipe.rank.exception;

import com.cheftory.api.exception.Error;
import lombok.Getter;

@Getter
public enum RecipeRankErrorCode implements Error {
    RECIPE_RANK_NOT_FOUND("RECIPE_RANK_001", "레시피 랭크를 찾을 수 없습니다.");
    final String errorCode;
    final String message;

    RecipeRankErrorCode(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
