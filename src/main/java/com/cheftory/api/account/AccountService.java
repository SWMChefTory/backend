package com.cheftory.api.account;

import com.cheftory.api.account.auth.exception.AuthErrorCode;
import com.cheftory.api.account.auth.exception.AuthException;
import com.cheftory.api.account.auth.model.AuthTokens;
import com.cheftory.api.account.auth.AuthService;
import com.cheftory.api.account.model.LoginResult;
import com.cheftory.api.account.model.UserInfo;
import com.cheftory.api.user.entity.Gender;
import com.cheftory.api.user.UserService;
import com.cheftory.api.user.entity.Provider;
import com.cheftory.api.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class AccountService {

  private final AuthService authService;
  private final UserService userService;

  public AccountService(AuthService authService, UserService userService) {
    this.authService = authService;
    this.userService = userService;
  }

  public LoginResult loginWithOAuth(String idToken, Provider provider) {
    String email = authService.extractEmailFromIdToken(idToken, provider);
    User user = userService.getByEmail(email);

    if (user.getProvider() != provider) {
      switch (user.getProvider()) {
        case GOOGLE -> throw new AuthException(AuthErrorCode.EMAIL_ALREADY_REGISTERED_WITH_GOOGLE);
        case APPLE -> throw new AuthException(AuthErrorCode.EMAIL_ALREADY_REGISTERED_WITH_APPLE);
        case KAKAO -> throw new AuthException(AuthErrorCode.EMAIL_ALREADY_REGISTERED_WITH_KAKAO);
        default -> throw new AuthException(AuthErrorCode.UNSUPPORTED_PROVIDER);
      }
    }

    UUID id = user.getId();
    String nickname = user.getNickname();

    AuthTokens authTokens = authService.createAuthToken(id);
    authService.saveRefreshToken(id, authTokens.refreshToken());

    return LoginResult.from(
        authTokens.accessToken(),
        authTokens.refreshToken(),
        UserInfo.from(
            id,
            email,
            nickname
        )
    );
  }

  public LoginResult signupWithOAuth(String idToken, Provider provider, String nickname,
      Gender gender) {
    String email = authService.extractEmailFromIdToken(idToken, provider);
    UUID id = userService.create(email, nickname, provider, gender);

    AuthTokens authTokens = authService.createAuthToken(id);
    authService.saveRefreshToken(id, authTokens.refreshToken());

    return LoginResult.from(
        authTokens.accessToken(),
        authTokens.refreshToken(),
        UserInfo.from(
            id,
            email,
            nickname
        )
    );
  }

  public void logout(String refreshToken) {
    UUID userId = authService.extractUserIdFromToken(refreshToken);
    authService.deleteRefreshToken(userId, refreshToken);
  }

  public void delete(String refreshToken) {
    UUID userId = authService.extractUserIdFromToken(refreshToken);
    authService.deleteRefreshToken(userId, refreshToken);
    userService.deleteUser(userId);
  }
}
