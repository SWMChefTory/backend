package com.cheftory.api.recipe.bookmark.exception;

import com.cheftory.api.exception.Error;
import com.cheftory.api.exception.ErrorType;

/**
 * 레시피 북마크 에러 코드
 */
public enum RecipeBookmarkErrorCode implements Error {
    RECIPE_BOOKMARK_NOT_FOUND("RECIPE_BOOKMARK_001", "유저 레시피가 존재하지 않습니다.", ErrorType.NOT_FOUND),
    RECIPE_BOOKMARK_ALREADY_EXISTS("RECIPE_BOOKMARK_002", "이미 존재하는 유저 레시피입니다.", ErrorType.CONFLICT),
    RECIPE_BOOKMARK_DUPLICATE("RECIPE_BOOKMARK_003", "이미 존재하는 유저 레시피입니다.", ErrorType.CONFLICT),
    RECIPE_BOOKMARK_CREATE_FAIL("RECIPE_BOOKMARK_004", "레시피 북마크 생성 실패", ErrorType.INTERNAL);
    private final String errorCode;
    private final String message;
    private final ErrorType type;

    RecipeBookmarkErrorCode(String errorCode, String message, ErrorType type) {
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
