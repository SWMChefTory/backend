package com.cheftory.api.exception;

import lombok.Getter;

@Getter
public class ExternalServerNetworkException extends RuntimeException {
  private final ExternalServerNetworkExceptionCode errorCode;

  public ExternalServerNetworkException(ExternalServerNetworkExceptionCode error) {
    this.errorCode = error;
  }
}
