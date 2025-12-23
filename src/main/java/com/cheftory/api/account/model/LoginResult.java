package com.cheftory.api.account.model;

public record LoginResult(String accessToken, String refreshToken, UserResponse userResponse) {
  public static LoginResult from(String accessToken, String refreshToken, UserResponse userResponse) {
    return new LoginResult(accessToken, refreshToken, userResponse);
  }
}
