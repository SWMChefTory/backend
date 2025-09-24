package com.cheftory.api.recipeinfo.recipe.exception;

import com.cheftory.api.recipeinfo.exception.RecipeInfoException;

public class RecipeException extends RecipeInfoException {
  public RecipeException(RecipeErrorCode errorCode) {
    super(errorCode);
  }
}
