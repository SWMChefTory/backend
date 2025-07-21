package com.cheftory.api.recipeviewstate.exception;

import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.exception.ErrorMessage;

public class RecipeViewStateException extends CheftoryException {

  public RecipeViewStateException(ErrorMessage errorMessage) {
    super(errorMessage);
  }
}
