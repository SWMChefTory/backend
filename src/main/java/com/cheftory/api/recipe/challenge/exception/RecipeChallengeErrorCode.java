package com.cheftory.api.recipe.challenge.exception;

import com.cheftory.api._common.PocOnly;
import com.cheftory.api.exception.Error;
import com.cheftory.api.exception.ErrorType;

@PocOnly(until = "2025-12-31")
public enum RecipeChallengeErrorCode implements Error {
    RECIPE_CHALLENGE_NOT_FOUND("RECIPE_CHALLENGE_001", "챌린지가 없습니다.", ErrorType.NOT_FOUND);
    private final String errorCode;
    private final String message;
    private final ErrorType type;

    RecipeChallengeErrorCode(String errorCode, String message, ErrorType type) {
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
