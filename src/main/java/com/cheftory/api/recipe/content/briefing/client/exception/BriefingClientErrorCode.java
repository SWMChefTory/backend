package com.cheftory.api.recipe.content.briefing.client.exception;

import com.cheftory.api.exception.ErrorMessage;

public enum BriefingClientErrorCode implements ErrorMessage {
  SERVER_ERROR("CAPTION_CLIENT_001", "briefing client와 통신하는 서버에서 오류가 발생했습니다.");
  final String errorCode;
  final String message;

  BriefingClientErrorCode(String errorCode, String message) {
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
