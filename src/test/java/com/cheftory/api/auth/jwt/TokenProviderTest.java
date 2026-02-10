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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("TokenProvider 테스트")
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

    @Nested
    @DisplayName("토큰 생성 (createToken)")
    class CreateToken {

        @Nested
        @DisplayName("Given - 액세스 토큰 타입일 때")
        class GivenAccessTokenType {

            @Test
            @DisplayName("Then - 유효한 액세스 토큰을 생성한다")
            void thenCreatesValidAccessToken() {
                String token = tokenProvider.createToken(userId, AuthTokenType.ACCESS);

                assertThat(token).isNotNull();
                assertThat(token).isNotBlank();
                assertThat(token.split("\\.")).hasSize(3);
            }
        }

        @Nested
        @DisplayName("Given - 리프레시 토큰 타입일 때")
        class GivenRefreshTokenType {

            @Test
            @DisplayName("Then - 유효한 리프레시 토큰을 생성한다")
            void thenCreatesValidRefreshToken() {
                String token = tokenProvider.createToken(userId, AuthTokenType.REFRESH);

                assertThat(token).isNotNull();
                assertThat(token).isNotBlank();
                assertThat(token.split("\\.")).hasSize(3);
            }
        }
    }

    @Nested
    @DisplayName("사용자 ID 추출 (getUserId)")
    class GetUserId {

        @Nested
        @DisplayName("Given - 유효한 액세스 토큰일 때")
        class GivenValidAccessToken {
            String token;

            @BeforeEach
            void setUp() {
                token = tokenProvider.createToken(userId, AuthTokenType.ACCESS);
            }

            @Test
            @DisplayName("Then - 사용자 ID를 반환한다")
            void thenReturnsUserId() throws AuthException {
                UUID extractedUserId = tokenProvider.getUserId(token, AuthTokenType.ACCESS);
                assertThat(extractedUserId).isEqualTo(userId);
            }
        }

        @Nested
        @DisplayName("Given - 잘못된 형식의 토큰일 때")
        class GivenMalformedToken {
            String malformedToken;

            @BeforeEach
            void setUp() {
                malformedToken = "not.a.valid.jwt.token";
            }

            @Test
            @DisplayName("Then - INVALID_TOKEN 예외를 던진다")
            void thenThrowsInvalidToken() {
                AuthException ex = assertThrows(
                        AuthException.class, () -> tokenProvider.getUserId(malformedToken, AuthTokenType.ACCESS));
                assertThat(ex.getError()).isEqualTo(AuthErrorCode.INVALID_TOKEN);
            }
        }

        @Nested
        @DisplayName("Given - 만료된 토큰일 때")
        class GivenExpiredToken {
            String expiredToken;

            @BeforeEach
            void setUp() {
                SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                Date now = new Date();
                Date oneSecondAgo = new Date(now.getTime() - 1000);

                expiredToken = io.jsonwebtoken.Jwts.builder()
                        .setSubject(userId.toString())
                        .claim("type", accessTokenType)
                        .setIssuedAt(oneSecondAgo)
                        .setExpiration(oneSecondAgo)
                        .signWith(secretKey)
                        .compact();
            }

            @Test
            @DisplayName("Then - EXPIRED_TOKEN 예외를 던진다")
            void thenThrowsExpiredToken() {
                AuthException ex = assertThrows(
                        AuthException.class, () -> tokenProvider.getUserId(expiredToken, AuthTokenType.ACCESS));
                assertThat(ex.getError()).isEqualTo(AuthErrorCode.EXPIRED_TOKEN);
            }
        }

        @Nested
        @DisplayName("Given - 서명이 잘못된 토큰일 때")
        class GivenInvalidSignatureToken {
            String tokenWithDifferentSignature;

            @BeforeEach
            void setUp() {
                String differentSecret = "different-secret-key-that-is-at-least-256-bits-long-for-hs256-algorithm";
                SecretKey differentKey = Keys.hmacShaKeyFor(differentSecret.getBytes(StandardCharsets.UTF_8));
                tokenWithDifferentSignature = io.jsonwebtoken.Jwts.builder()
                        .setSubject(userId.toString())
                        .claim("type", accessTokenType)
                        .setIssuedAt(new Date())
                        .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration * 1000))
                        .signWith(differentKey)
                        .compact();
            }

            @Test
            @DisplayName("Then - INVALID_TOKEN 예외를 던진다")
            void thenThrowsInvalidToken() {
                AuthException ex = assertThrows(
                        AuthException.class,
                        () -> tokenProvider.getUserId(tokenWithDifferentSignature, AuthTokenType.ACCESS));
                assertThat(ex.getError()).isEqualTo(AuthErrorCode.INVALID_TOKEN);
            }
        }

        @Nested
        @DisplayName("Given - 토큰 타입이 일치하지 않을 때")
        class GivenMismatchedTokenType {

            @Test
            @DisplayName("Then - 액세스 토큰으로 리프레시 토큰 검증 시 INVALID_REFRESH_TOKEN 예외를 던진다")
            void thenThrowsInvalidRefreshToken() {
                String accessToken = tokenProvider.createToken(userId, AuthTokenType.ACCESS);
                AuthException ex = assertThrows(
                        AuthException.class, () -> tokenProvider.getUserId(accessToken, AuthTokenType.REFRESH));
                assertThat(ex.getError()).isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
            }

            @Test
            @DisplayName("Then - 리프레시 토큰으로 액세스 토큰 검증 시 INVALID_ACCESS_TOKEN 예외를 던진다")
            void thenThrowsInvalidAccessToken() {
                String refreshToken = tokenProvider.createToken(userId, AuthTokenType.REFRESH);
                AuthException ex = assertThrows(
                        AuthException.class, () -> tokenProvider.getUserId(refreshToken, AuthTokenType.ACCESS));
                assertThat(ex.getError()).isEqualTo(AuthErrorCode.INVALID_ACCESS_TOKEN);
            }
        }
    }

    @Nested
    @DisplayName("만료 시간 추출 (getExpiration)")
    class GetExpiration {

        @Nested
        @DisplayName("Given - 유효한 액세스 토큰일 때")
        class GivenValidAccessToken {
            String token;

            @BeforeEach
            void setUp() {
                token = tokenProvider.createToken(userId, AuthTokenType.ACCESS);
            }

            @Test
            @DisplayName("Then - 만료 시간을 반환한다")
            void thenReturnsExpiration() throws AuthException {
                LocalDateTime expiration = tokenProvider.getExpiration(token);
                assertThat(expiration).isNotNull();
                assertThat(expiration).isAfter(LocalDateTime.now());
            }
        }

        @Nested
        @DisplayName("Given - 유효한 리프레시 토큰일 때")
        class GivenValidRefreshToken {
            String refreshToken;

            @BeforeEach
            void setUp() {
                refreshToken = tokenProvider.createToken(userId, AuthTokenType.REFRESH);
            }

            @Test
            @DisplayName("Then - 만료 시간을 반환한다")
            void thenReturnsExpiration() throws AuthException {
                LocalDateTime expiration = tokenProvider.getExpiration(refreshToken);
                assertThat(expiration).isNotNull();
                assertThat(expiration).isAfter(LocalDateTime.now());
            }
        }

        @Nested
        @DisplayName("Given - 잘못된 형식의 토큰일 때")
        class GivenMalformedToken {
            String malformedToken;

            @BeforeEach
            void setUp() {
                malformedToken = "not.a.valid.jwt.token";
            }

            @Test
            @DisplayName("Then - INVALID_TOKEN 예외를 던진다")
            void thenThrowsInvalidToken() {
                AuthException ex = assertThrows(AuthException.class, () -> tokenProvider.getExpiration(malformedToken));
                assertThat(ex.getError()).isEqualTo(AuthErrorCode.INVALID_TOKEN);
            }
        }
    }

    @Nested
    @DisplayName("토큰 만료 시간 검증")
    class TokenExpirationCheck {

        @Test
        @DisplayName("Then - 설정된 만료 시간과 일치해야 한다")
        void thenMatchesConfiguredExpiration() throws AuthException {
            String accessToken = tokenProvider.createToken(userId, AuthTokenType.ACCESS);
            String refreshToken = tokenProvider.createToken(userId, AuthTokenType.REFRESH);

            LocalDateTime accessExpiration = tokenProvider.getExpiration(accessToken);
            LocalDateTime refreshExpiration = tokenProvider.getExpiration(refreshToken);
            LocalDateTime now = LocalDateTime.now();

            long accessMinutesDiff =
                    java.time.Duration.between(now, accessExpiration).toMinutes();
            assertThat(accessMinutesDiff)
                    .isCloseTo(accessTokenExpiration / 60, org.assertj.core.data.Offset.offset(1L));

            long refreshDaysDiff =
                    java.time.Duration.between(now, refreshExpiration).toDays();
            assertThat(refreshDaysDiff)
                    .isCloseTo(refreshTokenExpiration / 86400, org.assertj.core.data.Offset.offset(1L));
        }
    }
}
