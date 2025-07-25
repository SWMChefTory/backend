package com.cheftory.api.account.auth.verifier.exception;

import com.cheftory.api.exception.ErrorMessage;
import lombok.Getter;

@Getter
public enum VerificationErrorCode implements ErrorMessage {

  // Apple 관련
  APPLE_INVALID_FORMAT("APPLE_001", "Apple 토큰 형식이 유효하지 않습니다."),
  APPLE_PUBLIC_KEY_NOT_FOUND("APPLE_002", "Apple 공개키를 찾을 수 없습니다."),
  APPLE_SIGNATURE_VERIFICATION_FAILED("APPLE_003", "Apple 토큰의 서명 검증에 실패했습니다."),
  APPLE_INVALID_ISSUER("APPLE_004", "Apple 토큰 발급자가 유효하지 않습니다."),
  APPLE_INVALID_AUDIENCE("APPLE_005", "Apple 토큰 대상 클라이언트 정보가 유효하지 않습니다."),
  APPLE_INVALID_ALGORITHM("APPLE_006", "Apple 토큰의 서명 알고리즘이 올바르지 않습니다."),
  APPLE_TOKEN_EXPIRED("APPLE_007", "Apple 토큰이 만료되었습니다."),

  // Google 관련
  GOOGLE_RESPONSE_NOT_OK("GOOGLE_001", "Google 토큰 검증 응답이 실패했습니다."),
  GOOGLE_MISSING_EMAIL("GOOGLE_002", "Google 토큰에 이메일 정보가 없습니다."),

  // 공통 오류 코드
  UNKNOWN_ERROR("COMMON_001", "알 수 없는 오류가 발생했습니다.");

  private final String code;
  private final String message;

  VerificationErrorCode(String code, String message) {
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