package com.cheftory.api.recipe.rank.exception;

import com.cheftory.api.exception.Error;
import com.cheftory.api.exception.ErrorType;
import lombok.Getter;

@Getter
public enum RecipeRankErrorCode implements Error {
    RECIPE_RANK_NOT_FOUND("RECIPE_RANK_001", "레시피 랭크를 찾을 수 없습니다.", ErrorType.NOT_FOUND),
    RECIPE_RANK_RECOMMEND_FAILED("RECIPE_RANK_002", "레시피 추천 랭킹 조회에 실패했습니다.", ErrorType.INTERNAL),
    RECIPE_RANK_EVENT_FAILED("RECIPE_RANK_003", "레시피 랭킹 이벤트 기록에 실패했습니다.", ErrorType.INTERNAL);
    final String errorCode;
    final String message;
    final ErrorType type;

    RecipeRankErrorCode(String errorCode, String message, ErrorType type) {
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
