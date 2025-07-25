package com.cheftory.api.account.auth.exception;

import com.cheftory.api.exception.ErrorMessage;

public enum AuthErrorCode implements ErrorMessage {

  INVALID_TOKEN("AUTH_001", "유효하지 않은 토큰입니다."),
  EXPIRED_TOKEN("AUTH_002", "만료된 토큰입니다."),
  INVALID_USER("AUTH_003", "유효하지 않은 사용자입니다."),
  MISSING_TOKEN("AUTH_004", "토큰이 누락되었습니다."),
  UNSUPPORTED_PROVIDER("AUTH_005", "지원하지 않는 인증 제공자입니다."),
  INVALID_ID_TOKEN("AUTH_006", "유효하지 않은 ID 토큰입니다."),
  INVALID_REFRESH_TOKEN("AUTH_007", "유효하지 않은 리프레시 토큰입니다."),

  EMAIL_ALREADY_REGISTERED_WITH_GOOGLE("AUTH_008", "이미 Google로 등록된 이메일입니다."),
  EMAIL_ALREADY_REGISTERED_WITH_APPLE("AUTH_009", "이미 Apple로 등록된 이메일입니다."),
  EMAIL_ALREADY_REGISTERED_WITH_KAKAO("AUTH_010", "이미 Kakao로 등록된 이메일입니다.");


  private final String code;
  private final String message;

  AuthErrorCode(String code, String message) {
    this.code = code;
    this.message = message;
  }

  @Override
  public String getErrorCode() {
    return code;
  }

  @Override
  public String getMessage() {
    return message;
  }
}
