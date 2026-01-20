package com.cheftory.api.recipe.category.exception;

import com.cheftory.api.recipe.exception.RecipeException;

public class RecipeCategoryException extends RecipeException {

    public RecipeCategoryException(RecipeCategoryErrorCode errorCode) {
        super(errorCode);
    }
}
