package com.cheftory.api.account;

import com.cheftory.api.account.model.Account;
import com.cheftory.api.auth.AuthService;
import com.cheftory.api.auth.entity.AuthTokenType;
import com.cheftory.api.auth.exception.AuthException;
import com.cheftory.api.auth.model.AuthTokens;
import com.cheftory.api.credit.CreditService;
import com.cheftory.api.credit.entity.Credit;
import com.cheftory.api.credit.exception.CreditException;
import com.cheftory.api.user.UserService;
import com.cheftory.api.user.entity.Gender;
import com.cheftory.api.user.entity.Provider;
import com.cheftory.api.user.entity.User;
import com.cheftory.api.user.exception.UserException;
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
    private final CreditService creditService;

    public Account login(String idToken, Provider provider) throws UserException, AuthException {
        String providerSub = authService.extractProviderSubFromIdToken(idToken, provider);
        User user = userService.get(provider, providerSub);

        AuthTokens authTokens = createAuthTokensAndSession(user.getId());

        return Account.of(authTokens.accessToken(), authTokens.refreshToken(), user);
    }

    public Account signup(
            String idToken,
            Provider provider,
            String nickname,
            Gender gender,
            LocalDate dateOfBirth,
            boolean isTermsOfUseAgreed,
            boolean isPrivacyPolicyAgreed,
            boolean isMarketingAgreed)
            throws UserException, AuthException, CreditException {
        String providerSub = authService.extractProviderSubFromIdToken(idToken, provider);
        User user = userService.create(
                nickname,
                gender,
                dateOfBirth,
                provider,
                providerSub,
                isTermsOfUseAgreed,
                isPrivacyPolicyAgreed,
                isMarketingAgreed);

        AuthTokens authTokens = createAuthTokensAndSession(user.getId());
        creditService.grant(Credit.signupBonus(user.getId()));

        return Account.of(authTokens.accessToken(), authTokens.refreshToken(), user);
    }

    public void logout(String refreshToken) throws AuthException {
        UUID userId = authService.extractUserIdFromToken(refreshToken, AuthTokenType.REFRESH);
        authService.deleteRefreshToken(userId, refreshToken);
    }

    public void delete(String refreshToken) throws AuthException, UserException {
        UUID userId = authService.extractUserIdFromToken(refreshToken, AuthTokenType.REFRESH);
        authService.deleteRefreshToken(userId, refreshToken);
        userService.delete(userId);
    }

    private AuthTokens createAuthTokensAndSession(UUID userId) throws AuthException {
        AuthTokens authTokens = authService.createAuthToken(userId);
        authService.saveLoginSession(userId, authTokens.refreshToken());
        return authTokens;
    }
}
