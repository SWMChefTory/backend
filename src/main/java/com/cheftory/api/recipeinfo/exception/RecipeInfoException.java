package com.cheftory.api.recipeinfo.exception;

import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.exception.ErrorMessage;

public class RecipeInfoException extends CheftoryException {

  public RecipeInfoException(ErrorMessage errorCode) {
    super(errorCode);
  }
}
