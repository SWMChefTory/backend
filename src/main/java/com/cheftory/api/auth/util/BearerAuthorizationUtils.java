package com.cheftory.api.auth.util;

import com.cheftory.api.auth.exception.AuthErrorCode;
import com.cheftory.api.auth.exception.AuthException;

/**
 * Bearer 토큰 스킴 처리 유틸리티
 */
public final class BearerAuthorizationUtils {

    private static final String BEARER_PREFIX = "Bearer ";

    private BearerAuthorizationUtils() {
        // 인스턴스 생성 방지
    }

    /**
     * Bearer 스킴 제거
     *
     * @param originalToken "Bearer " 접두사가 포함된 토큰
     * @return 접두사가 제거된 토큰
     * @throws AuthException 토큰이 null이거나 Bearer 스킴이 아닐 때 INVALID_TOKEN
     */
    public static String removePrefix(String originalToken) {
        if (originalToken == null || !originalToken.startsWith(BEARER_PREFIX)) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
        return originalToken.substring(BEARER_PREFIX.length());
    }

    /**
     * Bearer 스킴 추가
     *
     * @param token 접두사가 없는 토큰
     * @return "Bearer " 접두사가 추가된 토큰
     * @throws AuthException 토큰이 null이거나 비어있을 때 INVALID_TOKEN
     */
    public static String addPrefix(String token) {
        if (token == null || token.isEmpty()) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
        return BEARER_PREFIX + token;
    }
}
