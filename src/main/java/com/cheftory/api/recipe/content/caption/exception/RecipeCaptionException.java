package com.cheftory.api.recipe.content.caption.exception;

import com.cheftory.api.recipe.exception.RecipeException;

public class RecipeCaptionException extends RecipeException {
  public RecipeCaptionException(RecipeCaptionErrorCode errorCode) {
    super(errorCode);
  }
}
