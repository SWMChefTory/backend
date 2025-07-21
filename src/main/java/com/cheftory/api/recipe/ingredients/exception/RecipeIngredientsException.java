package com.cheftory.api.recipe.ingredients.exception;

import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.exception.ErrorMessage;
import com.cheftory.api.recipe.exception.RecipeErrorCode;

public class RecipeIngredientsException extends CheftoryException {
  public RecipeIngredientsException(RecipeIngredientsErrorCode errorCode) {
    super(errorCode);
  }
}
