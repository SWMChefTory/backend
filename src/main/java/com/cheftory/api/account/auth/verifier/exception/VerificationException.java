package com.cheftory.api.account.auth.verifier.exception;

import com.cheftory.api.exception.CheftoryException;
import lombok.Getter;

@Getter
public class VerificationException extends CheftoryException {

  public VerificationException(VerificationErrorCode errorCode) {
    super(errorCode);
  }
}