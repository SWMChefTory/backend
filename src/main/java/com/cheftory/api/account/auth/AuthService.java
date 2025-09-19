package com.cheftory.api.account.auth;

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
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

  private final GoogleTokenVerifier googleVerifier;
  private final AppleTokenVerifier appleVerifier;
  private final TokenProvider jwtProvider;
  private final LoginRepository loginRepository;

  public String extractProviderSubFromIdToken(String idToken, Provider provider) {
    try {
      return switch (provider) {
        case GOOGLE -> googleVerifier.getSubFromToken(idToken);
        case APPLE -> appleVerifier.getSubFromToken(idToken);
        default -> throw new AuthException(AuthErrorCode.UNSUPPORTED_PROVIDER);
      };
    } catch (Exception e) {
      throw new AuthException(AuthErrorCode.INVALID_ID_TOKEN);
    }
  }

  public UUID extractUserIdFromToken(String token) {
    return jwtProvider.getUserIdFromToken(token);
  }

  public AuthTokens reissue(String refreshToken) {

    if (!jwtProvider.isRefreshToken(refreshToken)) {
      throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }
    UUID userId = extractUserIdFromToken(refreshToken);

    AuthTokens authTokens = createAuthToken(userId);
    updateRefreshToken(userId, refreshToken, authTokens.refreshToken());
    return authTokens;
  }

  public AuthTokens createAuthToken(UUID userId) {
    String accessToken = jwtProvider.createAccessToken(userId);
    String refreshToken = jwtProvider.createRefreshToken(userId);
    return AuthTokens.of(accessToken, refreshToken);
  }

  public void saveLoginSession(UUID userId, String refreshToken) {
    LocalDateTime refreshTokenExpiredAt = jwtProvider.getExpiration(refreshToken);
    Login log = Login.create(userId, refreshToken, refreshTokenExpiredAt);
    loginRepository.save(log);
  }

  private void updateRefreshToken(UUID userId, String oldRefreshToken, String newRefreshToken) {
    Login log =
        loginRepository
            .findByUserIdAndRefreshToken(userId, oldRefreshToken)
            .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN));

    LocalDateTime refreshTokenExpiredAt = jwtProvider.getExpiration(newRefreshToken);
    log.updateRefreshToken(newRefreshToken, refreshTokenExpiredAt);
    loginRepository.save(log);
  }

  public void deleteRefreshToken(UUID userId, String refreshToken) {
    Login log =
        loginRepository
            .findByUserIdAndRefreshToken(userId, refreshToken)
            .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN));
    loginRepository.delete(log);
  }
}
