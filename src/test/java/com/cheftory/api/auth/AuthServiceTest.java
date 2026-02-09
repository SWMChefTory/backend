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
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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

    @Test
    void extractProviderSubFromIdToken_withGoogle_shouldReturnSub() throws VerificationException, AuthException {
        doReturn("google-sub").when(googleVerifier).getSubFromToken(idToken);

        String result = authService.extractProviderSubFromIdToken(idToken, Provider.GOOGLE);

        assertThat(result).isEqualTo("google-sub");
    }

    @Test
    void extractProviderSubFromIdToken_withApple_shouldReturnSub() throws VerificationException, AuthException {
        doReturn("apple-sub").when(appleVerifier).getSubFromToken(idToken);

        String result = authService.extractProviderSubFromIdToken(idToken, Provider.APPLE);

        assertThat(result).isEqualTo("apple-sub");
    }

    @Test
    void extractProviderSubFromIdToken_withInvalidToken_shouldThrow() throws VerificationException {
        com.cheftory.api.auth.verifier.exception.VerificationException verificationException =
                new com.cheftory.api.auth.verifier.exception.VerificationException(
                        com.cheftory.api.auth.verifier.exception.VerificationErrorCode.UNKNOWN_ERROR);
        doThrow(verificationException).when(googleVerifier).getSubFromToken(idToken);

        AuthException ex = assertThrows(
                AuthException.class, () -> authService.extractProviderSubFromIdToken(idToken, Provider.GOOGLE));

        assertThat(ex.getError()).isEqualTo(AuthErrorCode.INVALID_ID_TOKEN);
    }

    @Test
    void extractProviderSubFromIdToken_withNullProvider_shouldThrowUnsupportedProvider() {
        AuthException ex =
                assertThrows(AuthException.class, () -> authService.extractProviderSubFromIdToken(idToken, null));

        assertThat(ex.getError()).isEqualTo(AuthErrorCode.UNSUPPORTED_PROVIDER);
    }

    @Test
    void createAuthToken_shouldReturnTokens() {
        doReturn(accessToken).when(jwtProvider).createToken(userId, AuthTokenType.ACCESS);
        doReturn(refreshToken).when(jwtProvider).createToken(userId, AuthTokenType.REFRESH);

        AuthTokens result = authService.createAuthToken(userId);

        assertThat(result.accessToken()).isEqualTo(accessToken);
        assertThat(result.refreshToken()).isEqualTo(refreshToken);
    }

    @Test
    void saveLoginSession_shouldSaveWithCorrectExpirationAndCreatedAt() throws AuthException {
        doReturn(fixedNow).when(clock).now();

        LocalDateTime expiredAt = fixedNow.plusDays(7);
        doReturn(expiredAt).when(jwtProvider).getExpiration(refreshToken);

        authService.saveLoginSession(userId, refreshToken);

        ArgumentCaptor<Login> captor = ArgumentCaptor.forClass(Login.class);
        verify(loginRepository).create(captor.capture());
        Login saved = captor.getValue();

        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(saved.getRefreshTokenExpiredAt()).isEqualTo(expiredAt);
        assertThat(saved.getCreatedAt()).isEqualTo(fixedNow);
    }

    @Test
    void reissue_withInvalidToken_shouldThrow() throws AuthException {
        doThrow(new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN))
                .when(jwtProvider)
                .getUserId(refreshToken, AuthTokenType.REFRESH);

        AuthException ex = assertThrows(AuthException.class, () -> authService.reissue(refreshToken));

        assertThat(ex.getError()).isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void deleteRefreshToken_shouldDeleteLogin() throws AuthException {
        authService.deleteRefreshToken(userId, refreshToken);

        verify(loginRepository).delete(eq(userId), eq(refreshToken));
    }

    @Test
    void deleteRefreshToken_shouldThrowWhenNotFound() throws AuthException {
        AuthException exception = new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        doThrow(exception).when(loginRepository).delete(userId, refreshToken);

        AuthException ex =
                assertThrows(AuthException.class, () -> authService.deleteRefreshToken(userId, refreshToken));

        assertThat(ex.getError()).isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }
}
