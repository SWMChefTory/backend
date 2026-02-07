package com.cheftory.api.recipe.content.verify.exception;

import com.cheftory.api.recipe.exception.RecipeException;

public class RecipeVerifyException extends RecipeException {

    public RecipeVerifyException(RecipeVerifyErrorCode e) {
        super(e);
    }
}
