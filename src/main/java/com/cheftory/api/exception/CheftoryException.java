package com.cheftory.api.exception;

public class CheftoryException extends RuntimeException {
  private final String error;

  public CheftoryException(MessageError messageError) {
    super(messageError.getMessage());
    this.error = messageError.getError();
  }

  public String getError(){
    return error;
  }
}
