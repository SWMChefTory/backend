package com.cheftory.api.auth.jwt.property;

import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 설정 프로퍼티
 */
@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    /**
     * JWT 서명 키
     */
    private String secret;
    /**
     * 액세스 토큰 만료 시간 (초)
     */
    private long accessTokenExpiration;
    /**
     * 리프레시 토큰 만료 시간 (초)
     */
    private long refreshTokenExpiration;
    /**
     * 액세스 토큰 타입 식별자
     */
    private String accessTokenType;
    /**
     * 리프레시 토큰 타입 식별자
     */
    private String refreshTokenType;

    /**
     * Secret 키를 SecretKey로 변환
     *
     * @return HMAC-SHA 알고리즘용 SecretKey
     */
    public SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
