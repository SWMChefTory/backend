package com.cheftory.api.recipe.content.briefing.exception;

import com.cheftory.api.recipe.exception.RecipeException;

public class RecipeBriefingException extends RecipeException {

    public RecipeBriefingException(RecipeBriefingErrorCode e) {
        super(e);
    }
}
