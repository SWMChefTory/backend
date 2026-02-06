package com.cheftory.api.recipe.challenge.exception;

import com.cheftory.api._common.PocOnly;
import com.cheftory.api.exception.Error;

@PocOnly(until = "2025-12-31")
public enum RecipeChallengeErrorCode implements Error {
    RECIPE_CHALLENGE_NOT_FOUND("RECIPE_CHALLENGE_001", "챌린지가 없습니다.");
    private final String errorCode;
    private final String message;

    RecipeChallengeErrorCode(String errorCode, String message) {
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
