package com.cheftory.api.auth.model;

/**
 * 액세스 토큰과 리프레시 토큰을 담는 모델
 */
public record AuthTokens(String accessToken, String refreshToken) {

    /**
     * AuthTokens 인스턴스 생성
     *
     * @param accessToken 액세스 토큰
     * @param refreshToken 리프레시 토큰
     * @return AuthTokens 인스턴스
     */
    public static AuthTokens of(String accessToken, String refreshToken) {
        return new AuthTokens(accessToken, refreshToken);
    }
}
