package com.cheftory.api.recipe.helper;

import com.cheftory.api.recipe.entity.RecipeStatus;

public class RecipeBanException extends RuntimeException {
    public RecipeBanException(RecipeStatus recipeStatus) {
        super(recipeStatus.name());
    }
}
