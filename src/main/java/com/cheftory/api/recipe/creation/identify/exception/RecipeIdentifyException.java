package com.cheftory.api.recipe.creation.identify.exception;

import com.cheftory.api.recipe.exception.RecipeException;

public class RecipeIdentifyException extends RecipeException {
  public RecipeIdentifyException(RecipeIdentifyErrorCode errorCode) {
    super(errorCode);
  }
}
