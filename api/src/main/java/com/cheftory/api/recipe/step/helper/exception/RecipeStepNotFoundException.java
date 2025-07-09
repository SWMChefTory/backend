package com.cheftory.api.recipe.step.helper.exception;

public class RecipeStepNotFoundException extends RuntimeException {
    public RecipeStepNotFoundException(String message) {
        super(message);
    }
}
