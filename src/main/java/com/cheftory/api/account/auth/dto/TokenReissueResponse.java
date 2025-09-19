package com.cheftory.api.account.auth.dto;

import com.cheftory.api.account.auth.model.AuthTokens;
import com.cheftory.api.account.auth.util.BearerAuthorizationUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenReissueResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken) {

  public static TokenReissueResponse from(AuthTokens authTokens) {
    return new TokenReissueResponse(
        BearerAuthorizationUtils.addPrefix(authTokens.accessToken()),
        BearerAuthorizationUtils.addPrefix(authTokens.refreshToken()));
  }
}
