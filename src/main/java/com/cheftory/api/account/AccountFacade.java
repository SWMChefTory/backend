package com.cheftory.api.account;

import com.cheftory.api.account.model.Account;
import com.cheftory.api.auth.AuthService;
import com.cheftory.api.auth.model.AuthTokens;
import com.cheftory.api.user.UserService;
import com.cheftory.api.user.entity.Gender;
import com.cheftory.api.user.entity.Provider;
import com.cheftory.api.user.entity.User;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountFacade {

  private final AuthService authService;
  private final UserService userService;

  public Account login(String idToken, Provider provider) {
    String providerSub = authService.extractProviderSubFromIdToken(idToken, provider);
    User user = userService.getByProviderAndProviderSub(provider, providerSub);

    UUID id = user.getId();
    AuthTokens authTokens = authService.createAuthToken(id);
    authService.saveLoginSession(id, authTokens.refreshToken());

    return Account.of(
        authTokens.accessToken(),
        authTokens.refreshToken(),
        user);
  }

  public Account signup(
      String idToken,
      Provider provider,
      String nickname,
      Gender gender,
      LocalDate dateOfBirth,
      boolean isTermsOfUseAgreed,
      boolean isPrivacyPolicyAgreed,
      boolean isMarketingAgreed) {
    String providerSub = authService.extractProviderSubFromIdToken(idToken, provider);
    User user =
        userService.create(
            nickname,
            gender,
            dateOfBirth,
            provider,
            providerSub,
            isTermsOfUseAgreed,
            isPrivacyPolicyAgreed,
            isMarketingAgreed);

    AuthTokens authTokens = authService.createAuthToken(user.getId());
    authService.saveLoginSession(user.getId(), authTokens.refreshToken());

    return Account.of(
        authTokens.accessToken(),
        authTokens.refreshToken(),
        user);
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
