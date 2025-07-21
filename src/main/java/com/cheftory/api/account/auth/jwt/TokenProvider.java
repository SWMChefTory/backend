package com.cheftory.api.account.auth.jwt;

import com.cheftory.api.account.auth.exception.AuthErrorCode;
import com.cheftory.api.account.auth.exception.AuthException;
import com.cheftory.api.account.auth.jwt.property.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TokenProvider {

    private final JwtProperties jwtProperties;

    public String createAccessToken(UUID userId) {
        String token = createToken(userId, jwtProperties.getAccessTokenExpiration());
        return makeBearerPrefix(token);
    }

    public String createRefreshToken(UUID userId) {
        String token = createToken(userId, jwtProperties.getRefreshTokenExpiration());
        return makeBearerPrefix(token);
    }

    private String createToken(UUID userId, long expirySeconds) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirySeconds * 1000);

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(jwtProperties.getSecretKey())
                .compact();
    }

    private String removeBearerPrefix(String originalToken) {
        if (originalToken == null || !originalToken.startsWith("Bearer ")) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
        return originalToken.substring(7);
    }

    private String makeBearerPrefix(String token) {
        if (token == null || token.isEmpty()) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
        return "Bearer " + token;
    }

    public UUID getUserIdFromToken(String token) {
        String cleanedToken = removeBearerPrefix(token);
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtProperties.getSecretKey())
                    .build()
                    .parseClaimsJws(cleanedToken)
                    .getBody();

            if (claims.getExpiration().before(new Date())) {
                throw new AuthException(AuthErrorCode.EXPIRED_REFRESH_TOKEN);
            }

            return UUID.fromString(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
    }

    public LocalDateTime getExpiration(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtProperties.getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();

            return expiration.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
    }
}