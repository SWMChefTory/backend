package com.cheftory.api.account.dto;

import com.cheftory.api.account.auth.util.BearerAuthorizationUtils;
import com.cheftory.api.account.model.UserInfo;
import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken,
    @JsonProperty("user_info") UserInfo userInfo) {
  public static LoginResponse from(com.cheftory.api.account.model.LoginResult loginResult) {
    return new LoginResponse(
        BearerAuthorizationUtils.addPrefix(loginResult.accessToken()),
        BearerAuthorizationUtils.addPrefix(loginResult.refreshToken()),
        loginResult.userInfo());
  }
}
