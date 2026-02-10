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

/**
 * 계정 관련 비즈니스 로직을 처리하는 퍼사드 클래스.
 *
 * <p>로그인, 회원가입, 로그아웃, 회원 탈퇴 등 계정 관련 기능을 제공하며,
 * AuthService, UserService, CreditService를 조합하여 복합적인 작업을 수행합니다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountFacade {

    private final AuthService authService;
    private final UserService userService;
    private final CreditService creditService;

    /**
     * OAuth 로그인을 수행합니다.
     *
     * @param idToken OAuth ID 토큰
     * @param provider OAuth 제공자
     * @return 액세스 토큰, 리프레시 토큰, 사용자 정보를 포함한 계정 정보
     * @throws UserException 사용자 조회 실패 시
     * @throws AuthException 인증 처리 실패 시
     */
    public Account login(String idToken, Provider provider) throws UserException, AuthException {
        String providerSub = authService.extractProviderSubFromIdToken(idToken, provider);
        User user = userService.get(provider, providerSub);

        AuthTokens authTokens = createAuthTokensAndSession(user.getId());

        return Account.of(authTokens.accessToken(), authTokens.refreshToken(), user);
    }

    /**
     * OAuth 회원가입을 수행합니다.
     *
     * @param idToken OAuth ID 토큰
     * @param provider OAuth 제공자
     * @param nickname 사용자 닉네임
     * @param gender 성별
     * @param dateOfBirth 생년월일
     * @param isTermsOfUseAgreed 이용약관 동의 여부
     * @param isPrivacyPolicyAgreed 개인정보 처리방침 동의 여부
     * @param isMarketingAgreed 마케팅 정보 수신 동의 여부
     * @return 액세스 토큰, 리프레시 토큰, 사용자 정보를 포함한 계정 정보
     * @throws UserException 사용자 생성 실패 시
     * @throws AuthException 인증 처리 실패 시
     * @throws CreditException 크레딧 지급 실패 시
     */
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

    /**
     * 로그아웃을 수행합니다.
     *
     * @param refreshToken 리프레시 토큰
     * @throws AuthException 리프레시 토큰 삭제 실패 시
     */
    public void logout(String refreshToken) throws AuthException {
        UUID userId = authService.extractUserIdFromToken(refreshToken, AuthTokenType.REFRESH);
        authService.deleteRefreshToken(userId, refreshToken);
    }

    /**
     * 회원 탈퇴를 수행합니다.
     *
     * @param refreshToken 리프레시 토큰
     * @throws AuthException 리프레시 토큰 삭제 실패 시
     * @throws UserException 사용자 삭제 실패 시
     */
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
