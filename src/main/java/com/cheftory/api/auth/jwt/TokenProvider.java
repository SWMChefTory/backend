package com.cheftory.api.auth.jwt;

import com.cheftory.api.auth.exception.AuthErrorCode;
import com.cheftory.api.auth.exception.AuthException;
import com.cheftory.api.auth.jwt.property.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenProvider {

  private final JwtProperties jwtProperties;

  public String createAccessToken(UUID userId) {
    return createToken(
        userId, jwtProperties.getAccessTokenExpiration(), jwtProperties.getAccessTokenType());
  }

  public String createRefreshToken(UUID userId) {
    return createToken(
        userId, jwtProperties.getRefreshTokenExpiration(), jwtProperties.getRefreshTokenType());
  }

  private String createToken(UUID userId, long expirySeconds, String type) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + expirySeconds * 1000);

    return Jwts.builder()
        .setSubject(userId.toString())
        .claim("type", type)
        .setIssuedAt(now)
        .setExpiration(expiry)
        .signWith(jwtProperties.getSecretKey())
        .compact();
  }

  public UUID getUserIdFromToken(String token) {
    try {
      Claims claims =
          Jwts.parserBuilder()
              .setSigningKey(jwtProperties.getSecretKey())
              .build()
              .parseClaimsJws(token)
              .getBody();

      if (claims.getExpiration().before(new Date())) {
        throw new AuthException(AuthErrorCode.EXPIRED_TOKEN);
      }

      return UUID.fromString(claims.getSubject());
    } catch (Exception e) {
      throw new AuthException(AuthErrorCode.INVALID_TOKEN);
    }
  }

  public boolean isRefreshToken(String token) {
    try {
      JwtParser parser = Jwts.parserBuilder().setSigningKey(jwtProperties.getSecretKey()).build();

      Claims claims = parser.parseClaimsJws(token).getBody();
      String tokenType = claims.get("type", String.class);
      return jwtProperties.getRefreshTokenType().equals(tokenType);
    } catch (Exception e) {
      log.error("토큰 검증 실패: {}", e.getMessage());
      throw new AuthException(AuthErrorCode.INVALID_TOKEN);
    }
  }

  public LocalDateTime getExpiration(String token) {
    try {
      Claims claims =
          Jwts.parserBuilder()
              .setSigningKey(jwtProperties.getSecretKey())
              .build()
              .parseClaimsJws(token)
              .getBody();

      Date expiration = claims.getExpiration();

      return expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

    } catch (Exception e) {
      throw new AuthException(AuthErrorCode.INVALID_TOKEN);
    }
  }
}
