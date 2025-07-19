package com.cheftory.api.exception;

import lombok.Getter;



@Getter
public enum GlobalErrorCode implements ErrorCode {

  FIELD_REQUIRED("GLOBAL_1", "필수 필드가 누락되었습니다.");

  private final String code;
  private final String message;

  GlobalErrorCode(String code, String message) {
    this.code = code;
    this.message = message;
  }
}