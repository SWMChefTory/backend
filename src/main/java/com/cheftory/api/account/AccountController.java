package com.cheftory.api.account;

import com.cheftory.api._common.reponse.SuccessOnlyResponse;
import com.cheftory.api._common.security.UserPrincipal;
import com.cheftory.api.account.auth.util.BearerAuthorizationUtils;
import com.cheftory.api.account.dto.*;
import com.cheftory.api.account.model.LoginResult;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/account")
public class AccountController {

  private final AccountService accountService;

  @PostMapping("/login/oauth")
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
        request.dateOfBirth()
    );
    return LoginResponse.from(result);
  }

  @PostMapping("/logout")
  public SuccessOnlyResponse logout(@RequestBody LogoutRequest request) {
    accountService.logout(BearerAuthorizationUtils.removePrefix(request.refreshToken()));
    return SuccessOnlyResponse.create();
  }

  @DeleteMapping
  public SuccessOnlyResponse delete(@RequestBody LogoutRequest request) {
    accountService.delete(BearerAuthorizationUtils.removePrefix(request.refreshToken()));
    return SuccessOnlyResponse.create();
  }

  @GetMapping("/error")
  public void getError(@UserPrincipal UUID userId) {
    throw new RuntimeException("error");
  }
}
