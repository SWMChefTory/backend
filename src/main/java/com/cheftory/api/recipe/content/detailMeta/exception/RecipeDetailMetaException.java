package com.cheftory.api.recipe.content.detailMeta.exception;

import com.cheftory.api.recipe.exception.RecipeException;

public class RecipeDetailMetaException extends RecipeException {

    public RecipeDetailMetaException(RecipeDetailMetaErrorCode e) {
        super(e);
    }
}
