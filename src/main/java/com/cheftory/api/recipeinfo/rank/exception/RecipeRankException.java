package com.cheftory.api.recipeinfo.rank.exception;

import com.cheftory.api.recipeinfo.exception.RecipeInfoException;

public class RecipeRankException extends RecipeInfoException {
  public RecipeRankException(RecipeRankErrorCode errorCode) {
    super(errorCode);
  }
}
