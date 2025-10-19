package com.cheftory.api.recipeinfo.history.exception;

import com.cheftory.api.exception.ErrorMessage;
import com.cheftory.api.recipeinfo.exception.RecipeInfoException;

public class RecipeHistoryException extends RecipeInfoException {

  public RecipeHistoryException(ErrorMessage errorMessage) {
    super(errorMessage);
  }
}
