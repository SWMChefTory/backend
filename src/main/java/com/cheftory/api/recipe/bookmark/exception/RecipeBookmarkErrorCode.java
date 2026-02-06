package com.cheftory.api.recipe.bookmark.exception;

import com.cheftory.api.exception.Error;

public enum RecipeBookmarkErrorCode implements Error {
    RECIPE_BOOKMARK_NOT_FOUND("RECIPE_BOOKMARK_001", "유저 레시피가 존재하지 않습니다."),
    RECIPE_BOOKMARK_ALREADY_EXISTS("RECIPE_BOOKMARK_002", "이미 존재하는 유저 레시피입니다."),
    RECIPE_BOOKMARK_DUPLICATE("RECIPE_BOOKMARK_003", "이미 존재하는 유저 레시피입니다.");
    private final String errorCode;
    private final String message;

    RecipeBookmarkErrorCode(String errorCode, String message) {
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
