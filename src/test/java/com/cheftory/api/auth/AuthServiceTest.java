package com.cheftory.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.cheftory.api._common.Clock;
import com.cheftory.api.auth.entity.AuthTokenType;
import com.cheftory.api.auth.entity.Login;
import com.cheftory.api.auth.exception.AuthErrorCode;
import com.cheftory.api.auth.exception.AuthException;
import com.cheftory.api.auth.jwt.TokenProvider;
import com.cheftory.api.auth.model.AuthTokens;
import com.cheftory.api.auth.repository.LoginRepository;
import com.cheftory.api.auth.verifier.AppleTokenVerifier;
import com.cheftory.api.auth.verifier.GoogleTokenVerifier;
import com.cheftory.api.auth.verifier.exception.VerificationException;
import com.cheftory.api.user.entity.Provider;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@DisplayName("AuthService 테스트")
class AuthServiceTest {

    private GoogleTokenVerifier googleVerifier;
    private AppleTokenVerifier appleVerifier;
    private TokenProvider jwtProvider;
    private LoginRepository loginRepository;
    private Clock clock;

    private AuthService authService;

    private final String idToken = "dummy-id-token";
    private final String refreshToken = "refresh-token";
    private final String accessToken = "access-token";
    private final UUID userId = UUID.randomUUID();
    private final LocalDateTime fixedNow = LocalDateTime.of(2025, 1, 1, 0, 0);

    @BeforeEach
    void setUp() {
        googleVerifier = mock(GoogleTokenVerifier.class);
        appleVerifier = mock(AppleTokenVerifier.class);
        jwtProvider = mock(TokenProvider.class);
        loginRepository = mock(LoginRepository.class);
        clock = mock(Clock.class);

        authService = new AuthService(googleVerifier, appleVerifier, jwtProvider, loginRepository, clock);
    }

    @Nested
    @DisplayName("Provider Sub 추출 (extractProviderSubFromIdToken)")
    class ExtractProviderSub {

        @Nested
        @DisplayName("Given - Google Provider일 때")
        class GivenGoogle {

            @BeforeEach
            void setUp() throws VerificationException {
                doReturn("google-sub").when(googleVerifier).getSubFromToken(idToken);
            }

            @Nested
            @DisplayName("When - 추출을 요청하면")
            class WhenExtracting {
                String result;

                @BeforeEach
                void setUp() throws AuthException {
                    result = authService.extractProviderSubFromIdToken(idToken, Provider.GOOGLE);
                }

                @Test
                @DisplayName("Then - Google Sub를 반환한다")
                void thenReturnsGoogleSub() {
                    assertThat(result).isEqualTo("google-sub");
                }
            }
        }

        @Nested
        @DisplayName("Given - Apple Provider일 때")
        class GivenApple {

            @BeforeEach
            void setUp() throws VerificationException {
                doReturn("apple-sub").when(appleVerifier).getSubFromToken(idToken);
            }

            @Nested
            @DisplayName("When - 추출을 요청하면")
            class WhenExtracting {
                String result;

                @BeforeEach
                void setUp() throws AuthException {
                    result = authService.extractProviderSubFromIdToken(idToken, Provider.APPLE);
                }

                @Test
                @DisplayName("Then - Apple Sub를 반환한다")
                void thenReturnsAppleSub() {
                    assertThat(result).isEqualTo("apple-sub");
                }
            }
        }

        @Nested
        @DisplayName("Given - 유효하지 않은 토큰일 때")
        class GivenInvalidToken {

            @BeforeEach
            void setUp() throws VerificationException {
                com.cheftory.api.auth.verifier.exception.VerificationException verificationException =
                        new com.cheftory.api.auth.verifier.exception.VerificationException(
                                com.cheftory.api.auth.verifier.exception.VerificationErrorCode.UNKNOWN_ERROR);
                doThrow(verificationException).when(googleVerifier).getSubFromToken(idToken);
            }

            @Nested
            @DisplayName("When - 추출을 요청하면")
            class WhenExtracting {

                @Test
                @DisplayName("Then - INVALID_ID_TOKEN 예외를 던진다")
                void thenThrowsException() {
                    AuthException ex = assertThrows(
                            AuthException.class,
                            () -> authService.extractProviderSubFromIdToken(idToken, Provider.GOOGLE));
                    assertThat(ex.getError()).isEqualTo(AuthErrorCode.INVALID_ID_TOKEN);
                }
            }
        }

        @Nested
        @DisplayName("Given - Provider가 null일 때")
        class GivenNullProvider {

            @Nested
            @DisplayName("When - 추출을 요청하면")
            class WhenExtracting {

                @Test
                @DisplayName("Then - UNSUPPORTED_PROVIDER 예외를 던진다")
                void thenThrowsException() {
                    AuthException ex = assertThrows(
                            AuthException.class, () -> authService.extractProviderSubFromIdToken(idToken, null));
                    assertThat(ex.getError()).isEqualTo(AuthErrorCode.UNSUPPORTED_PROVIDER);
                }
            }
        }
    }

    @Nested
    @DisplayName("인증 토큰 생성 (createAuthToken)")
    class CreateAuthToken {

        @Nested
        @DisplayName("Given - 사용자 ID가 주어졌을 때")
        class GivenUserId {

            @BeforeEach
            void setUp() {
                doReturn(accessToken).when(jwtProvider).createToken(userId, AuthTokenType.ACCESS);
                doReturn(refreshToken).when(jwtProvider).createToken(userId, AuthTokenType.REFRESH);
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {
                AuthTokens result;

                @BeforeEach
                void setUp() {
                    result = authService.createAuthToken(userId);
                }

                @Test
                @DisplayName("Then - 액세스 토큰과 리프레시 토큰을 반환한다")
                void thenReturnsTokens() {
                    assertThat(result.accessToken()).isEqualTo(accessToken);
                    assertThat(result.refreshToken()).isEqualTo(refreshToken);
                }
            }
        }
    }

    @Nested
    @DisplayName("로그인 세션 저장 (saveLoginSession)")
    class SaveLoginSession {

        @Nested
        @DisplayName("Given - 사용자 ID와 리프레시 토큰이 주어졌을 때")
        class GivenUserIdAndToken {
            LocalDateTime expiredAt;

            @BeforeEach
            void setUp() throws AuthException {
                doReturn(fixedNow).when(clock).now();
                expiredAt = fixedNow.plusDays(7);
                doReturn(expiredAt).when(jwtProvider).getExpiration(refreshToken);
            }

            @Nested
            @DisplayName("When - 저장을 요청하면")
            class WhenSaving {

                @BeforeEach
                void setUp() throws AuthException {
                    authService.saveLoginSession(userId, refreshToken);
                }

                @Test
                @DisplayName("Then - 올바른 정보로 로그인을 저장한다")
                void thenSavesLogin() {
                    ArgumentCaptor<Login> captor = ArgumentCaptor.forClass(Login.class);
                    verify(loginRepository).create(captor.capture());
                    Login saved = captor.getValue();

                    assertThat(saved.getUserId()).isEqualTo(userId);
                    assertThat(saved.getRefreshToken()).isEqualTo(refreshToken);
                    assertThat(saved.getRefreshTokenExpiredAt()).isEqualTo(expiredAt);
                    assertThat(saved.getCreatedAt()).isEqualTo(fixedNow);
                }
            }
        }
    }

    @Nested
    @DisplayName("토큰 재발급 (reissue)")
    class Reissue {

        @Nested
        @DisplayName("Given - 유효하지 않은 리프레시 토큰일 때")
        class GivenInvalidToken {

            @BeforeEach
            void setUp() throws AuthException {
                doThrow(new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN))
                        .when(jwtProvider)
                        .getUserId(refreshToken, AuthTokenType.REFRESH);
            }

            @Nested
            @DisplayName("When - 재발급을 요청하면")
            class WhenReissuing {

                @Test
                @DisplayName("Then - INVALID_REFRESH_TOKEN 예외를 던진다")
                void thenThrowsException() {
                    AuthException ex = assertThrows(AuthException.class, () -> authService.reissue(refreshToken));
                    assertThat(ex.getError()).isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
                }
            }
        }
    }

    @Nested
    @DisplayName("리프레시 토큰 삭제 (deleteRefreshToken)")
    class DeleteRefreshToken {

        @Nested
        @DisplayName("Given - 존재하는 토큰일 때")
        class GivenExistingToken {

            @Nested
            @DisplayName("When - 삭제를 요청하면")
            class WhenDeleting {

                @BeforeEach
                void setUp() throws AuthException {
                    authService.deleteRefreshToken(userId, refreshToken);
                }

                @Test
                @DisplayName("Then - 로그인을 삭제한다")
                void thenDeletesLogin() throws AuthException {
                    verify(loginRepository).delete(eq(userId), eq(refreshToken));
                }
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 토큰일 때")
        class GivenNonExistingToken {

            @BeforeEach
            void setUp() throws AuthException {
                AuthException exception = new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
                doThrow(exception).when(loginRepository).delete(userId, refreshToken);
            }

            @Nested
            @DisplayName("When - 삭제를 요청하면")
            class WhenDeleting {

                @Test
                @DisplayName("Then - INVALID_REFRESH_TOKEN 예외를 던진다")
                void thenThrowsException() {
                    AuthException ex = assertThrows(
                            AuthException.class, () -> authService.deleteRefreshToken(userId, refreshToken));
                    assertThat(ex.getError()).isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
                }
            }
        }
    }
}
