package com.cheftory.api.recipe.search.exception;

import com.cheftory.api.recipe.exception.RecipeException;

public class RecipeSearchException extends RecipeException {
    public RecipeSearchException(RecipeSearchErrorCode errorCode) {
        super(errorCode);
    }
}
