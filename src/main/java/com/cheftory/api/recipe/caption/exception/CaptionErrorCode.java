package com.cheftory.api.recipe.caption.exception;

import com.cheftory.api.exception.ErrorMessage;
import lombok.Getter;

@Getter
public enum CaptionErrorCode implements ErrorMessage {
  CAPTION_NOT_FOUND("CAPTION_001","자막이 존재하지 않습니다.");
  final String errorCode;
  final String message;

  CaptionErrorCode(String errorCode,String message) {
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
