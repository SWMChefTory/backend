package com.cheftory.api.auth.util;

import com.cheftory.api.auth.exception.AuthErrorCode;
import com.cheftory.api.auth.exception.AuthException;

public final class BearerAuthorizationUtils {

  private static final String BEARER_PREFIX = "Bearer ";

  private BearerAuthorizationUtils() {
    // 인스턴스 생성 방지
  }

  public static String removePrefix(String originalToken) {
    if (originalToken == null || !originalToken.startsWith(BEARER_PREFIX)) {
      throw new AuthException(AuthErrorCode.INVALID_TOKEN);
    }
    return originalToken.substring(BEARER_PREFIX.length());
  }

  public static String addPrefix(String token) {
    if (token == null || token.isEmpty()) {
      throw new AuthException(AuthErrorCode.INVALID_TOKEN);
    }
    return BEARER_PREFIX + token;
  }
}
