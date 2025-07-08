package com.cheftory.api.recipe;

public class CannotCreateException extends RuntimeException {
  public CannotCreateException(String message) {
    super(message);
  }
}
