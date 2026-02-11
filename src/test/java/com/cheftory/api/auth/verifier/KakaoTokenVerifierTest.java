package com.cheftory.api.auth.verifier;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("KakaoTokenVerifier 테스트")
class KakaoTokenVerifierTest {

    private final KakaoTokenVerifier kakaoTokenVerifier = new KakaoTokenVerifier();

    @Nested
    @DisplayName("getEmailFromToken 메서드")
    class GetEmailFromToken {

        @Test
        @DisplayName("토큰을 받으면 null을 반환한다 (미구현)")
        void returnsNull() {
            String token = "kakao-access-token";

            String result = kakaoTokenVerifier.getEmailFromToken(token);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("null 토큰도 null을 반환한다 (미구현)")
        void returnsNullForNullToken() {
            String result = kakaoTokenVerifier.getEmailFromToken(null);

            assertThat(result).isNull();
        }
    }
}
