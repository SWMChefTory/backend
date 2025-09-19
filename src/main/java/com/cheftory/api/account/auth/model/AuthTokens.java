package com.cheftory.api.account.auth.model;

public record AuthTokens(String accessToken, String refreshToken) {

  public static AuthTokens of(String accessToken, String refreshToken) {
    return new AuthTokens(accessToken, refreshToken);
  }
}
