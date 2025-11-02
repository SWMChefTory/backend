package com.cheftory.api.affiliate.coupang.exception;

import com.cheftory.api.exception.ErrorMessage;

public enum CoupangErrorCode implements ErrorMessage {
  COUPANG_API_REQUEST_FAIL("COUPANG_001", "쿠팡 API 요청에 실패했습니다.");

  final String errorCode;
  final String message;

  CoupangErrorCode(String errorCode, String message) {
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
