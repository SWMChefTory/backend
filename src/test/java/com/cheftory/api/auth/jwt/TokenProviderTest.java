package com.cheftory.api.auth.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cheftory.api._common.Clock;
import com.cheftory.api.auth.entity.AuthTokenType;
import com.cheftory.api.auth.exception.AuthErrorCode;
import com.cheftory.api.auth.exception.AuthException;
import com.cheftory.api.auth.jwt.property.JwtProperties;
import io.jsonwebtoken.security.Keys;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TokenProviderTest {

    private TokenProvider tokenProvider;
    private JwtProperties jwtProperties;
    private Clock clock;

    private final String secret = "test-secret-key-that-is-at-least-256-bits-long-for-hs256-algorithm";
    private final long accessTokenExpiration = 3600L;
    private final long refreshTokenExpiration = 604800L;
    private final String accessTokenType = "access";
    private final String refreshTokenType = "refresh";
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        clock = Mockito.mock(Clock.class);
        Mockito.when(clock.nowMillis()).thenAnswer(i -> System.currentTimeMillis());

        jwtProperties = new JwtProperties();

        Field secretField = JwtProperties.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(jwtProperties, secret);

        Field accessTokenExpirationField = JwtProperties.class.getDeclaredField("accessTokenExpiration");
        accessTokenExpirationField.setAccessible(true);
        accessTokenExpirationField.set(jwtProperties, accessTokenExpiration);

        Field refreshTokenExpirationField = JwtProperties.class.getDeclaredField("refreshTokenExpiration");
        refreshTokenExpirationField.setAccessible(true);
        refreshTokenExpirationField.set(jwtProperties, refreshTokenExpiration);

        Field accessTokenTypeField = JwtProperties.class.getDeclaredField("accessTokenType");
        accessTokenTypeField.setAccessible(true);
        accessTokenTypeField.set(jwtProperties, accessTokenType);

        Field refreshTokenTypeField = JwtProperties.class.getDeclaredField("refreshTokenType");
        refreshTokenTypeField.setAccessible(true);
        refreshTokenTypeField.set(jwtProperties, refreshTokenType);

        tokenProvider = new TokenProvider(jwtProperties, clock);
    }

    @Test
    void createAccessToken_shouldReturnValidToken() {
        String token = tokenProvider.createToken(userId, AuthTokenType.ACCESS);

        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void createRefreshToken_shouldReturnValidToken() {
        String token = tokenProvider.createToken(userId, AuthTokenType.REFRESH);

        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void getUserIdFromToken_withValidAccessToken_shouldReturnUserId() {
        String token = tokenProvider.createToken(userId, AuthTokenType.ACCESS);

        UUID extractedUserId = tokenProvider.getUserId(token, AuthTokenType.ACCESS);

        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    void getUserId_withMalformedToken_shouldThrowInvalidToken() {
        String malformedToken = "not.a.valid.jwt.token";

        AuthException ex =
                assertThrows(AuthException.class, () -> tokenProvider.getUserId(malformedToken, AuthTokenType.ACCESS));

        assertThat(ex.getError()).isEqualTo(AuthErrorCode.INVALID_TOKEN);
    }

    @Test
    void getUserId_withExpiredToken_shouldThrowExpiredToken() {
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date oneSecondAgo = new Date(now.getTime() - 1000);

        String expiredToken = io.jsonwebtoken.Jwts.builder()
                .setSubject(userId.toString())
                .claim("type", accessTokenType)
                .setIssuedAt(oneSecondAgo)
                .setExpiration(oneSecondAgo)
                .signWith(secretKey)
                .compact();

        AuthException ex =
                assertThrows(AuthException.class, () -> tokenProvider.getUserId(expiredToken, AuthTokenType.ACCESS));

        assertThat(ex.getError()).isEqualTo(AuthErrorCode.EXPIRED_TOKEN);
    }

    @Test
    void getExpiration_withValidToken_shouldReturnExpirationDateTime() {
        String token = tokenProvider.createToken(userId, AuthTokenType.ACCESS);

        LocalDateTime expiration = tokenProvider.getExpiration(token);

        assertThat(expiration).isNotNull();
        assertThat(expiration).isAfter(LocalDateTime.now());
    }

    @Test
    void getExpiration_withRefreshToken_shouldReturnExpirationDateTime() {
        String refreshToken = tokenProvider.createToken(userId, AuthTokenType.REFRESH);

        LocalDateTime expiration = tokenProvider.getExpiration(refreshToken);

        assertThat(expiration).isNotNull();
        assertThat(expiration).isAfter(LocalDateTime.now());
    }

    @Test
    void getExpiration_withMalformedToken_shouldThrowInvalidToken() {
        String malformedToken = "not.a.valid.jwt.token";

        AuthException ex = assertThrows(AuthException.class, () -> tokenProvider.getExpiration(malformedToken));

        assertThat(ex.getError()).isEqualTo(AuthErrorCode.INVALID_TOKEN);
    }

    @Test
    void getUserIdFromToken_and_getExpiration_shouldUseSameExpirationTime() {
        String token = tokenProvider.createToken(userId, AuthTokenType.ACCESS);

        UUID extractedUserId = tokenProvider.getUserId(token, AuthTokenType.ACCESS);
        LocalDateTime expiration = tokenProvider.getExpiration(token);

        assertThat(extractedUserId).isEqualTo(userId);
        assertThat(expiration).isNotNull();
        assertThat(expiration).isAfter(LocalDateTime.now());
    }

    @Test
    void getUserId_withInvalidSignature_shouldThrowInvalidToken() {
        String differentSecret = "different-secret-key-that-is-at-least-256-bits-long-for-hs256-algorithm";
        SecretKey differentKey = Keys.hmacShaKeyFor(differentSecret.getBytes(StandardCharsets.UTF_8));
        String tokenWithDifferentSignature = io.jsonwebtoken.Jwts.builder()
                .setSubject(userId.toString())
                .claim("type", accessTokenType)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration * 1000))
                .signWith(differentKey)
                .compact();

        AuthException ex = assertThrows(
                AuthException.class, () -> tokenProvider.getUserId(tokenWithDifferentSignature, AuthTokenType.ACCESS));

        assertThat(ex.getError()).isEqualTo(AuthErrorCode.INVALID_TOKEN);
    }

    @Test
    void tokenExpiration_shouldMatchConfiguredExpiration() {
        String accessToken = tokenProvider.createToken(userId, AuthTokenType.ACCESS);
        String refreshToken = tokenProvider.createToken(userId, AuthTokenType.REFRESH);

        LocalDateTime accessExpiration = tokenProvider.getExpiration(accessToken);
        LocalDateTime refreshExpiration = tokenProvider.getExpiration(refreshToken);
        LocalDateTime now = LocalDateTime.now();

        long accessMinutesDiff =
                java.time.Duration.between(now, accessExpiration).toMinutes();
        assertThat(accessMinutesDiff).isCloseTo(accessTokenExpiration / 60, org.assertj.core.data.Offset.offset(1L));

        long refreshDaysDiff =
                java.time.Duration.between(now, refreshExpiration).toDays();
        assertThat(refreshDaysDiff).isCloseTo(refreshTokenExpiration / 86400, org.assertj.core.data.Offset.offset(1L));
    }
}
