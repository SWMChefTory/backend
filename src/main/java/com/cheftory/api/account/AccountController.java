package com.cheftory.api.account;

import com.cheftory.api.account.dto.*;
import com.cheftory.api.account.auth.model.AuthToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/account")
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/login/oauth")
    public ResponseEntity<LoginResponse> loginWithOAuth(@RequestBody LoginRequest request) {
        AuthToken authToken = accountService.loginWithOAuth(request.getToken(), request.getProvider());
        return ResponseEntity.ok(LoginResponse.from(authToken));
    }

    @PostMapping("/signup/oauth")
    public ResponseEntity<LoginResponse> signupWithOAuth(@RequestBody SignupRequest request) {
        AuthToken authToken = accountService.signupWithOAuth(request.getToken(), request.getProvider(), request.getNickname(), request.getGender());
        return ResponseEntity.ok(LoginResponse.from(authToken));
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        AuthToken authToken = accountService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(LoginResponse.from(authToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody LogoutRequest request) {
        accountService.logout(request.getRefreshToken());
        return ResponseEntity.ok("Successfully logged out");
    }

    @PostMapping("/delete")
    public ResponseEntity<String> delete(@RequestBody LogoutRequest request) {
        accountService.delete(request.getRefreshToken());
        return ResponseEntity.ok("Successfully deleted account");
    }
}
