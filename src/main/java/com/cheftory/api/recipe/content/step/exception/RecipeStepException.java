package com.cheftory.api.recipe.content.step.exception;

import com.cheftory.api.recipe.exception.RecipeException;

public class RecipeStepException extends RecipeException {

    public RecipeStepException(RecipeStepErrorCode e) {
        super(e);
    }
}
