package com.cheftory.api.recipeinfo.viewstatus.exception;

import com.cheftory.api.exception.ErrorMessage;
import com.cheftory.api.recipeinfo.exception.RecipeInfoException;

public class ViewStatusException extends RecipeInfoException {

  public ViewStatusException(ErrorMessage errorMessage) {
    super(errorMessage);
  }
}
