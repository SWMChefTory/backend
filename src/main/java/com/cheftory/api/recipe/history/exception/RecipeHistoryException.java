package com.cheftory.api.recipe.history.exception;

import com.cheftory.api.exception.ErrorMessage;
import com.cheftory.api.recipe.exception.RecipeException;

public class RecipeHistoryException extends RecipeException {

  public RecipeHistoryException(ErrorMessage errorMessage) {
    super(errorMessage);
  }
}
