package com.cheftory.api.recipe.step.helper;

import java.util.UUID;

public class RecipeStepNotFoundException extends RuntimeException{
    public RecipeStepNotFoundException(String message) {
        super(message);
    }
}
