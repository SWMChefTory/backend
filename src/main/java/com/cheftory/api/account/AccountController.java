package com.cheftory.api.account;

import com.cheftory.api.account.auth.util.BearerAuthorizationUtils;
import com.cheftory.api.account.dto.*;
import com.cheftory.api.account.model.LoginResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/account")
public class AccountController {

  private final AccountService accountService;

  @PostMapping("/signin/oauth")
  public LoginResponse loginWithOAuth(@RequestBody LoginRequest request) {
    LoginResult result = accountService.loginWithOAuth(request.idToken(), request.provider());
    return LoginResponse.from(result);
  }

  @PostMapping("/signup/oauth")
  public LoginResponse signupWithOAuth(@RequestBody SignupRequest request) {
    LoginResult result = accountService.signupWithOAuth(
        request.idToken(),
        request.provider(),
        request.nickname(),
        request.gender(),
        request.birthOfDate()
    );
    return LoginResponse.from(result);
  }

  @PostMapping("/logout")
  public String logout(@RequestBody LogoutRequest request) {
    accountService.logout(BearerAuthorizationUtils.removePrefix(request.refreshToken()));
    return "Successfully logged out";
  }

  @PostMapping("/delete")
  public String delete(@RequestBody LogoutRequest request) {
    accountService.delete(BearerAuthorizationUtils.removePrefix(request.refreshToken()));
    return "Successfully deleted account";
  }
}
