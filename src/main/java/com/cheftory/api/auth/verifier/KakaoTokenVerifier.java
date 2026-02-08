package com.cheftory.api.auth.verifier;

import org.springframework.stereotype.Component;

/**
 * Kakao OAuth 토큰 검증기
 */
@Component
public class KakaoTokenVerifier {
    /**
     * Kakao 토큰에서 이메일 추출
     *
     * @param token Kakao 토큰
     * @return 이메일 주소 (TODO: 구현 필요)
     */
    public String getEmailFromToken(String token) {
        // TODO: implement Kakao token validation
        return null;
    }
}
