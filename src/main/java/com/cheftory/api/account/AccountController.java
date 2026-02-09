package com.cheftory.api.account;

import com.cheftory.api._common.reponse.SuccessOnlyResponse;
import com.cheftory.api.account.dto.LoginRequest;
import com.cheftory.api.account.dto.LoginResponse;
import com.cheftory.api.account.dto.LogoutRequest;
import com.cheftory.api.account.dto.SignupRequest;
import com.cheftory.api.account.model.Account;
import com.cheftory.api.auth.exception.AuthException;
import com.cheftory.api.auth.util.BearerAuthorizationUtils;
import com.cheftory.api.credit.exception.CreditException;
import com.cheftory.api.user.exception.UserException;
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

    private final AccountFacade facade;

    @PostMapping("/login/oauth")
    public LoginResponse loginWithOAuth(@RequestBody LoginRequest request) throws AuthException, UserException {
        Account account = facade.login(request.idToken(), request.provider());
        return LoginResponse.from(account);
    }

    @PostMapping("/signup/oauth")
    public LoginResponse signupWithOAuth(@RequestBody SignupRequest request)
            throws AuthException, UserException, CreditException {
        Account account = facade.signup(
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
    public SuccessOnlyResponse logout(@RequestBody LogoutRequest request) throws AuthException {
        facade.logout(BearerAuthorizationUtils.removePrefix(request.refreshToken()));
        return SuccessOnlyResponse.create();
    }

    @DeleteMapping
    public SuccessOnlyResponse delete(@RequestBody LogoutRequest request) throws AuthException, UserException {
        facade.delete(BearerAuthorizationUtils.removePrefix(request.refreshToken()));
        return SuccessOnlyResponse.create();
    }
}
