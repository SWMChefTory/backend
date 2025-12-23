package com.cheftory.api.user.exception;

import com.cheftory.api.exception.CheftoryException;

public class UserException extends CheftoryException {

  public UserException(UserErrorCode errorCode) {
    super(errorCode);
  }
}
