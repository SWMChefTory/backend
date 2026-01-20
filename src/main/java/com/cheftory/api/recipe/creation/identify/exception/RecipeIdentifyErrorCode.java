package com.cheftory.api.recipe.creation.identify.exception;

import com.cheftory.api.exception.ErrorMessage;
import lombok.Getter;

@Getter
public enum RecipeIdentifyErrorCode implements ErrorMessage {
    RECIPE_IDENTIFY_PROGRESSING("RECIPE_IDENTIFY_001", "레시피 생성이 진행중입니다.");
    final String errorCode;
    final String message;

    RecipeIdentifyErrorCode(String errorCode, String message) {
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
