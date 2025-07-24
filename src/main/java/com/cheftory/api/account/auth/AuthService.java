package com.cheftory.api.account.auth;

import com.cheftory.api.account.auth.exception.AuthErrorCode;
import com.cheftory.api.account.auth.exception.AuthException;
import com.cheftory.api.account.auth.model.AuthToken;
import com.cheftory.api.account.auth.entity.LoggedIn;
import com.cheftory.api.account.auth.verifier.AppleTokenVerifier;
import com.cheftory.api.account.auth.verifier.GoogleTokenVerifier;
import com.cheftory.api.account.auth.jwt.TokenProvider;
import com.cheftory.api.account.auth.repository.LoggedInRepository;
import com.cheftory.api.user.entity.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final GoogleTokenVerifier googleVerifier;
  private final AppleTokenVerifier appleVerifier;
  private final TokenProvider jwtProvider;
  private final LoggedInRepository loggedInRepository;

  public String extractEmailFromOAuthToken(String token, Provider provider) {
    try {
      return switch (provider) {
        case GOOGLE -> googleVerifier.getEmailFromToken(token);
        case APPLE -> appleVerifier.getEmailFromToken(token);
        default -> throw new UnsupportedOperationException("Unsupported provider: " + provider);
      };
    } catch (Throwable e) {
      throw new AuthException(AuthErrorCode.INVALID_TOKEN);
    }
  }

  public UUID extractUserIdFromToken(String token) {
    return jwtProvider.getUserIdFromToken(token);
  }

  public AuthToken createAuthToken(UUID userId) {
    String accessToken = jwtProvider.createAccessToken(userId);
    String refreshToken = jwtProvider.createRefreshToken(userId);
    return AuthToken.of(accessToken, refreshToken);
  }

  public void saveRefreshToken(UUID userId, String refreshToken) {
    LocalDateTime refreshTokenExpiredAt = jwtProvider.getExpiration(refreshToken);
    LoggedIn log = LoggedIn.create(userId, refreshToken, refreshTokenExpiredAt);
    loggedInRepository.save(log);
  }

  public void updateRefreshToken(UUID userId, String oldRefreshToken, String newRefreshToken) {
    LoggedIn log = loggedInRepository.findByUserIdAndRefreshToken(userId, oldRefreshToken)
        .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_TOKEN));

    LocalDateTime refreshTokenExpiredAt = jwtProvider.getExpiration(newRefreshToken);
    log.updateRefreshToken(newRefreshToken, refreshTokenExpiredAt);
    loggedInRepository.save(log);
  }

  public void deleteRefreshToken(UUID userId, String refreshToken) {
    LoggedIn log = loggedInRepository.findByUserIdAndRefreshToken(userId, refreshToken)
        .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_TOKEN));
    loggedInRepository.delete(log);
  }
}