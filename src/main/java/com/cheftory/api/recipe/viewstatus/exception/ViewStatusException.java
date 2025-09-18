package com.cheftory.api.recipe.viewstatus.exception;

import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.exception.ErrorMessage;

public class ViewStatusException extends CheftoryException {

  public ViewStatusException(ErrorMessage errorMessage) {
    super(errorMessage);
  }
}
