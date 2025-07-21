package com.cheftory.api.exception;


public enum GlobalErrorCode implements ErrorMessage {

  FIELD_REQUIRED("GLOBAL_1", "필수 필드가 누락되었습니다."),
  UNKNOWN_ERROR("GLOBAL_2", "알 수 없는 오류가 발생했습니다.");

  private final String errorCode;
  private final String message;

  GlobalErrorCode(String errorCode, String message) {
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