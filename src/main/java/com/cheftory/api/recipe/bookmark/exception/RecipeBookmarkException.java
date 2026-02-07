package com.cheftory.api.recipe.bookmark.exception;

import com.cheftory.api.recipe.exception.RecipeException;

public class RecipeBookmarkException extends RecipeException {

    public RecipeBookmarkException(RecipeBookmarkErrorCode errorCode) {
        super(errorCode);
    }
}
