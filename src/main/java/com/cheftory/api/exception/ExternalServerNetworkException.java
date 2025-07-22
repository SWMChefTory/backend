package com.cheftory.api.exception;

import lombok.Getter;

@Getter
public class ExternalServerNetworkException extends RuntimeException {
  private ExternalServerNetworkExceptionCode errorCode;

  public ExternalServerNetworkException(ExternalServerNetworkExceptionCode error) {
    this.errorCode = error;
  }
}
