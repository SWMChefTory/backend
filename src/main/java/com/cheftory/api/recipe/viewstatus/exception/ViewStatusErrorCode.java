package com.cheftory.api.recipe.viewstatus.exception;

import com.cheftory.api.exception.ErrorMessage;

public enum ViewStatusErrorCode implements ErrorMessage {
  VIEW_STATUS_NOT_FOUND("VIEW_STATUS_001","유저 레시피가 존재하지 않습니다.");
  private final String errorCode;
  private final String message;

  ViewStatusErrorCode(String errorCode, String message) {
    this.errorCode = errorCode;
    this.message = message;
  }

  @Override
  public String getErrorCode() {
    return errorCode;
  }

  @Override
  public String getMessage() {
    return message;
  }
}
