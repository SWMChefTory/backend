package com.cheftory.api.recipe.rank.exception;

import com.cheftory.api.recipe.exception.RecipeException;

public class RecipeRankException extends RecipeException {
    public RecipeRankException(RecipeRankErrorCode errorCode) {
        super(errorCode);
    }
}
