package com.cheftory.api.recipe.content.detailMeta.exception;

import com.cheftory.api.exception.ErrorMessage;

public enum RecipeDetailMetaErrorCode implements ErrorMessage {
    DETAIL_META_NOT_FOUND("RECIPE_DETAIL_META_001", "레시피 상세 조회에 실패 했습니다."),
    ;
    final String errorCode;
    final String message;

    RecipeDetailMetaErrorCode(String errorCode, String message) {
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
