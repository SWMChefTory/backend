package com.cheftory.api.recipeinfo.caption.exception;

import com.cheftory.api.recipeinfo.exception.RecipeInfoException;

public class RecipeCaptionException extends RecipeInfoException {
  public RecipeCaptionException(RecipeCaptionErrorCode errorCode) {
    super(errorCode);
  }
}
