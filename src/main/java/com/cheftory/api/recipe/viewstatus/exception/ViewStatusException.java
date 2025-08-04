package com.cheftory.api.recipe.watched.viewstatus.exception;

import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.exception.ErrorMessage;

public class ViewStatusException extends CheftoryException {

  public ViewStatusException(ErrorMessage errorMessage) {
    super(errorMessage);
  }
}
