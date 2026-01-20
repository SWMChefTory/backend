package com.cheftory.api.search.exception;

import com.cheftory.api.recipe.exception.RecipeException;

public class SearchException extends RecipeException {
    public SearchException(SearchErrorCode errorCode) {
        super(errorCode);
    }
}
