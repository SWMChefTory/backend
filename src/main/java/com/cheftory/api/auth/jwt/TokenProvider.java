package com.cheftory.api.auth.jwt;

import com.cheftory.api._common.Clock;
import com.cheftory.api.auth.entity.AuthTokenType;
import com.cheftory.api.auth.exception.AuthErrorCode;
import com.cheftory.api.auth.exception.AuthException;
import com.cheftory.api.auth.jwt.property.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * JWT 토큰 생성 및 검증을 담당하는 프로바이더
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenProvider {

    private final JwtProperties jwtProperties;
    private final Clock clock;

    /**
     * JWT 토큰 생성
     *
     * @param userId 유저 ID
     * @param type 토큰 타입 (ACCESS, REFRESH)
     * @return 생성된 JWT 토큰
     */
    public String createToken(UUID userId, AuthTokenType type) {
        long expirySeconds = type == AuthTokenType.ACCESS
                ? jwtProperties.getAccessTokenExpiration()
                : jwtProperties.getRefreshTokenExpiration();
        String typeString =
                type == AuthTokenType.ACCESS ? jwtProperties.getAccessTokenType() : jwtProperties.getRefreshTokenType();

        Date now = new Date(clock.nowMillis());
        Date expiry = new Date(clock.nowMillis() + expirySeconds * 1000);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("type", typeString)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(jwtProperties.getSecretKey())
                .compact();
    }

    /**
     * 토큰에서 유저 ID 추출
     *
     * @param token 검증할 JWT 토큰
     * @param expectedType 예상 토큰 타입 (ACCESS, REFRESH)
     * @return 토큰에 포함된 유저 ID
     * @throws AuthException 토큰 타입이 일치하지 않을 때 INVALID_ACCESS_TOKEN 또는 INVALID_REFRESH_TOKEN
     * @throws AuthException 토큰이 만료되었을 때 EXPIRED_TOKEN
     * @throws AuthException 토큰이 유효하지 않을 때 INVALID_TOKEN
     */
    public UUID getUserId(String token, AuthTokenType expectedType) throws AuthException {
        Claims claims = parseTokenClaims(token);

        String expected = expectedType == AuthTokenType.ACCESS
                ? jwtProperties.getAccessTokenType()
                : jwtProperties.getRefreshTokenType();

        if (!expected.equals(claims.get("type", String.class))) {
            throw new AuthException(
                    expectedType == AuthTokenType.ACCESS
                            ? AuthErrorCode.INVALID_ACCESS_TOKEN
                            : AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        return UUID.fromString(claims.getSubject());
    }

    /**
     * 토큰 만료 시간 추출
     *
     * @param token 검증할 JWT 토큰
     * @return 토큰 만료 시간
     * @throws AuthException 토큰이 만료되었을 때 EXPIRED_TOKEN
     * @throws AuthException 토큰이 유효하지 않을 때 INVALID_TOKEN
     */
    public LocalDateTime getExpiration(String token) throws AuthException {
        Claims claims = parseTokenClaims(token);
        Date expiration = claims.getExpiration();
        return expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * 토큰 클레임 파싱
     *
     * @param token 파싱할 JWT 토큰
     * @return 파싱된 클레임
     * @throws AuthException 토큰이 만료되었을 때 EXPIRED_TOKEN
     * @throws AuthException 토큰이 유효하지 않을 때 INVALID_TOKEN
     */
    private Claims parseTokenClaims(String token) throws AuthException {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(jwtProperties.getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new AuthException(AuthErrorCode.EXPIRED_TOKEN);
        } catch (Exception e) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
    }
}
