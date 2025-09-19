package com.cheftory.api.account.auth;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.cheftory.api.account.auth.entity.Login;
import com.cheftory.api.account.auth.exception.AuthErrorCode;
import com.cheftory.api.account.auth.exception.AuthException;
import com.cheftory.api.account.auth.jwt.TokenProvider;
import com.cheftory.api.account.auth.model.AuthTokens;
import com.cheftory.api.account.auth.repository.LoginRepository;
import com.cheftory.api.account.auth.verifier.AppleTokenVerifier;
import com.cheftory.api.account.auth.verifier.GoogleTokenVerifier;
import com.cheftory.api.account.user.entity.Provider;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock GoogleTokenVerifier googleVerifier;
  @Mock AppleTokenVerifier appleVerifier;
  @Mock TokenProvider jwtProvider;
  @Mock LoginRepository loginRepository;
  @InjectMocks AuthService authService;

  private final String idToken = "dummy-id-token";
  private final String refreshToken = "refresh-token";
  private final String newRefreshToken = "new-refresh-token";
  private final String accessToken = "access-token";
  private final UUID userId = UUID.randomUUID();

  @Test
  void extractProviderSubFromIdToken_withGoogle_shouldReturnSub() {
    doReturn("google-sub").when(googleVerifier).getSubFromToken(idToken);

    String result = authService.extractProviderSubFromIdToken(idToken, Provider.GOOGLE);

    assertThat(result).isEqualTo("google-sub");
  }

  @Test
  void extractProviderSubFromIdToken_withApple_shouldReturnSub() {
    doReturn("apple-sub").when(appleVerifier).getSubFromToken(idToken);

    String result = authService.extractProviderSubFromIdToken(idToken, Provider.APPLE);

    assertThat(result).isEqualTo("apple-sub");
  }

  @Test
  void extractProviderSubFromIdToken_withInvalidToken_shouldThrow() {
    doThrow(RuntimeException.class).when(googleVerifier).getSubFromToken(idToken);

    AuthException ex =
        assertThrows(
            AuthException.class,
            () -> authService.extractProviderSubFromIdToken(idToken, Provider.GOOGLE));

    assertThat(ex.getErrorMessage()).isEqualTo(AuthErrorCode.INVALID_ID_TOKEN);
  }

  @Test
  void extractProviderSubFromIdToken_withUnsupportedProvider_shouldThrow() {
    AuthException ex =
        assertThrows(
            AuthException.class, () -> authService.extractProviderSubFromIdToken(idToken, null));

    assertThat(ex.getErrorMessage()).isEqualTo(AuthErrorCode.INVALID_ID_TOKEN);
  }

  @Test
  void createAuthToken_shouldReturnTokens() {
    doReturn(accessToken).when(jwtProvider).createAccessToken(userId);
    doReturn(refreshToken).when(jwtProvider).createRefreshToken(userId);

    AuthTokens result = authService.createAuthToken(userId);

    assertThat(result.accessToken()).isEqualTo(accessToken);
    assertThat(result.refreshToken()).isEqualTo(refreshToken);
  }

  @Test
  void saveLoginSession_shouldSaveWithCorrectExpiration() {
    LocalDateTime expiredAt = LocalDateTime.now().plusDays(7);
    doReturn(expiredAt).when(jwtProvider).getExpiration(refreshToken);

    authService.saveLoginSession(userId, refreshToken);

    ArgumentCaptor<Login> captor = ArgumentCaptor.forClass(Login.class);
    verify(loginRepository).save(captor.capture());
    Login saved = captor.getValue();

    assertThat(saved.getUserId()).isEqualTo(userId);
    assertThat(saved.getRefreshToken()).isEqualTo(refreshToken);
    assertThat(saved.getRefreshTokenExpiredAt()).isEqualTo(expiredAt);
  }

  @Test
  void reissue_shouldUpdateAndReturnNewTokens() {
    Login existingLogin = Login.create(userId, refreshToken, LocalDateTime.now().plusDays(1));

    doReturn(true).when(jwtProvider).isRefreshToken(refreshToken);
    doReturn(userId).when(jwtProvider).getUserIdFromToken(refreshToken);
    doReturn(accessToken).when(jwtProvider).createAccessToken(userId);
    doReturn(newRefreshToken).when(jwtProvider).createRefreshToken(userId);
    doReturn(Optional.of(existingLogin))
        .when(loginRepository)
        .findByUserIdAndRefreshToken(userId, refreshToken);
    doReturn(LocalDateTime.now().plusDays(7)).when(jwtProvider).getExpiration(newRefreshToken);

    AuthTokens result = authService.reissue(refreshToken);

    assertThat(result.accessToken()).isEqualTo(accessToken);
    assertThat(result.refreshToken()).isEqualTo(newRefreshToken);
    verify(loginRepository).save(any(Login.class));
  }

  @Test
  void reissue_withInvalidToken_shouldThrow() {
    doReturn(false).when(jwtProvider).isRefreshToken(refreshToken);

    AuthException ex = assertThrows(AuthException.class, () -> authService.reissue(refreshToken));

    assertThat(ex.getErrorMessage()).isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
  }

  @Test
  void deleteRefreshToken_shouldDeleteLogin() {
    Login login = Login.create(userId, refreshToken, LocalDateTime.now());
    doReturn(Optional.of(login))
        .when(loginRepository)
        .findByUserIdAndRefreshToken(userId, refreshToken);

    authService.deleteRefreshToken(userId, refreshToken);

    verify(loginRepository).delete(login);
  }

  @Test
  void deleteRefreshToken_shouldThrowWhenNotFound() {
    doReturn(Optional.empty())
        .when(loginRepository)
        .findByUserIdAndRefreshToken(userId, refreshToken);

    AuthException ex =
        assertThrows(
            AuthException.class, () -> authService.deleteRefreshToken(userId, refreshToken));

    assertThat(ex.getErrorMessage()).isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
  }
}
