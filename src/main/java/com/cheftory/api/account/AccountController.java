package com.cheftory.api.account;

import com.cheftory.api._common.reponse.SuccessOnlyResponse;
import com.cheftory.api.account.model.Account;
import com.cheftory.api.auth.util.BearerAuthorizationUtils;
import com.cheftory.api.account.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/account")
public class AccountController {

  private final AccountFacade accountFacade;

  @PostMapping("/login/oauth")
  public LoginResponse loginWithOAuth(@RequestBody LoginRequest request) {
    Account account = accountFacade.login(request.idToken(), request.provider());
    return LoginResponse.from(account);
  }

  @PostMapping("/signup/oauth")
  public LoginResponse signupWithOAuth(@RequestBody SignupRequest request) {
    Account account =
        accountFacade.signup(
            request.idToken(),
            request.provider(),
            request.nickname(),
            request.gender(),
            request.dateOfBirth(),
            request.isTermsOfUseAgreed(),
            request.isPrivacyAgreed(),
            request.isMarketingAgreed());
    return LoginResponse.from(account);
  }

  @PostMapping("/logout")
  public SuccessOnlyResponse logout(@RequestBody LogoutRequest request) {
    accountFacade.logout(BearerAuthorizationUtils.removePrefix(request.refreshToken()));
    return SuccessOnlyResponse.create();
  }

  @DeleteMapping
  public SuccessOnlyResponse delete(@RequestBody LogoutRequest request) {
    accountFacade.delete(BearerAuthorizationUtils.removePrefix(request.refreshToken()));
    return SuccessOnlyResponse.create();
  }
}
