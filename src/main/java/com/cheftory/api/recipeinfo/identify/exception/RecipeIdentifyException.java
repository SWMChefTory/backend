package com.cheftory.api.recipeinfo.identify.exception;

import com.cheftory.api.recipeinfo.exception.RecipeInfoException;

public class RecipeIdentifyException extends RecipeInfoException {
  public RecipeIdentifyException(RecipeIdentifyErrorCode errorCode) {
    super(errorCode);
  }
}
