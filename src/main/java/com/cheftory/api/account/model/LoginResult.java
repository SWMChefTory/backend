package com.cheftory.api.account.model;

public record LoginResult(
    String accessToken,
    String refreshToken,
    UserInfo userInfo
) {
  public static LoginResult from(String accessToken, String refreshToken, UserInfo userInfo) {
    return new LoginResult(accessToken, refreshToken, userInfo);
  }
}