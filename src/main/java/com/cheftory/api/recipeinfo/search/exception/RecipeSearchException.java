package com.cheftory.api.recipeinfo.search.exception;

import com.cheftory.api.recipeinfo.exception.RecipeInfoException;

public class RecipeSearchException extends RecipeInfoException {
  public RecipeSearchException(RecipeSearchErrorCode errorCode) {
    super(errorCode);
  }
}
