package com.cheftory.api.account.dto;

import com.cheftory.api.account.auth.model.AuthToken;
import lombok.Getter;

@Getter
public class LoginResponse {
    private final String accessToken;
    private final String refreshToken;

    private LoginResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static LoginResponse from(AuthToken token) {
        return new LoginResponse(token.getAccessToken(), token.getRefreshToken());
    }
}
