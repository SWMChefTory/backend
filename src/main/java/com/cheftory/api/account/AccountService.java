package com.cheftory.api.account;

import com.cheftory.api.account.auth.model.AuthTokens;
import com.cheftory.api.account.auth.AuthService;
import com.cheftory.api.account.model.LoginResult;
import com.cheftory.api.account.model.UserInfo;
import com.cheftory.api.account.user.entity.Gender;
import com.cheftory.api.account.user.UserService;
import com.cheftory.api.account.user.entity.Provider;
import com.cheftory.api.account.user.entity.User;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

  private final AuthService authService;
  private final UserService userService;

  public LoginResult loginWithOAuth(String idToken, Provider provider) {
    String providerSub = authService.extractProviderSubFromIdToken(idToken, provider);
    User user = userService.getByProviderAndProviderSub(provider, providerSub);

    UUID id = user.getId();
    String email = user.getEmail();
    String nickname = user.getNickname();
    Gender gender = user.getGender();
    LocalDate dateOfBirth = user.getDateOfBirth();

    AuthTokens authTokens = authService.createAuthToken(id);
    authService.saveLoginSession(id, authTokens.refreshToken());

    return LoginResult.from(
        authTokens.accessToken(),
        authTokens.refreshToken(),
        UserInfo.from(
            id,
            email,
            nickname,
            gender,
            dateOfBirth
        )
    );
  }

  public LoginResult signupWithOAuth(
      String idToken,
      Provider provider,
      String nickname,
      Gender gender,
      LocalDate dateOfBirth
  ) {
    String email = authService.extractEmailFromIdToken(idToken, provider);
    String providerSub = authService.extractProviderSubFromIdToken(idToken, provider);
    UUID id = userService.create(email, nickname, gender, dateOfBirth, provider, providerSub);

    AuthTokens authTokens = authService.createAuthToken(id);
    authService.saveLoginSession(id, authTokens.refreshToken());

    return LoginResult.from(
        authTokens.accessToken(),
        authTokens.refreshToken(),
        UserInfo.from(
            id,
            email,
            nickname,
            gender,
            dateOfBirth
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
