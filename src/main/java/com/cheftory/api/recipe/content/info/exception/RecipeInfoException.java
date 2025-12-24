package com.cheftory.api.recipe.content.info.exception;

public class RecipeInfoException extends com.cheftory.api.recipe.exception.RecipeException {
  public RecipeInfoException(RecipeInfoErrorCode errorCode) {
    super(errorCode);
  }
}
