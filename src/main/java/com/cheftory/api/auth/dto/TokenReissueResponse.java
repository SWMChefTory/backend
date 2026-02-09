package com.cheftory.api.auth.dto;

import com.cheftory.api.auth.exception.AuthException;
import com.cheftory.api.auth.model.AuthTokens;
import com.cheftory.api.auth.util.BearerAuthorizationUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 토큰 재발급 응답 모델
 */
public record TokenReissueResponse(
        @JsonProperty("access_token") String accessToken, @JsonProperty("refresh_token") String refreshToken) {

    /**
     * AuthTokens에서 TokenReissueResponse 인스턴스 생성
     *
     * @param authTokens 액세스 토큰과 리프레시 토큰
     * @return TokenReissueResponse 인스턴스
     */
    public static TokenReissueResponse from(AuthTokens authTokens) throws AuthException {
        return new TokenReissueResponse(
                BearerAuthorizationUtils.addPrefix(authTokens.accessToken()),
                BearerAuthorizationUtils.addPrefix(authTokens.refreshToken()));
    }
}
