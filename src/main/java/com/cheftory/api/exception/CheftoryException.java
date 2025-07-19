package com.cheftory.api.exception;

import com.cheftory.api.recipe.caption.errorcode.CaptionErrorCode;

public class CheftoryException extends RuntimeException {
  private final ErrorMessage errorMessage;


  public CheftoryException(ErrorMessage errorMessage) {
      this.errorMessage = errorMessage;
  }

  public ErrorMessage getErrorMessage() {
    return errorMessage;
  }
}
