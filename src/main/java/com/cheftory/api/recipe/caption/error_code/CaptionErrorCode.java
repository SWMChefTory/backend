package com.cheftory.api.recipe.caption.error_code;

import com.cheftory.api.exception.MessageError;

public enum CaptionErrorCode implements MessageError {
  CAPTION_NOT_FOUND("자막이 존재하지 않습니다.","");
  final String message;
  final String error;

  CaptionErrorCode(String message, String error) {
    this.message = message;
    this.error = error;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public String getError() {
    return error;
  }
}
