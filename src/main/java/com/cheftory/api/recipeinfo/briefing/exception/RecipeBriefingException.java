package com.cheftory.api.recipeinfo.briefing.exception;

import com.cheftory.api.recipeinfo.exception.RecipeInfoException;

public class RecipeBriefingException extends RecipeInfoException {

  public RecipeBriefingException(RecipeBriefingErrorCode e) {
    super(e);
  }
}
