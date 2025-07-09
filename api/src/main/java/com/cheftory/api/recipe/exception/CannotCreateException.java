package com.cheftory.api.recipe.exception;

public class CannotCreateException extends RuntimeException {
  public CannotCreateException(String message) {
    super(message);
  }
}
