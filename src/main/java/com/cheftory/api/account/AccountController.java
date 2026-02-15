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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 계정 관련 API 요청을 처리하는 컨트롤러.
 *
 * <p>로그인, 회원가입, 로그아웃, 회원 탈퇴 엔드포인트를 제공합니다.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/account")
public class AccountController {

    private final AccountFacade facade;

    /**
     * OAuth 로그인을 수행합니다.
     *
     * @param request 로그인 요청 정보 (idToken, provider)
     * @return 액세스 토큰, 리프레시 토큰, 사용자 정보
     * @throws AuthException 인증 처리 실패 시
     * @throws UserException 사용자 조회 실패 시
     */
    @PostMapping("/login/oauth")
    public LoginResponse loginWithOAuth(@RequestBody LoginRequest request) throws AuthException, UserException {
        Account account = facade.login(request.idToken(), request.provider());
        return LoginResponse.from(account);
    }

    /**
     * OAuth 회원가입을 수행합니다.
     *
     * @param request 회원가입 요청 정보 (idToken, provider, nickname, gender, dateOfBirth, 동의 여부들)
     * @return 액세스 토큰, 리프레시 토큰, 사용자 정보
     * @throws AuthException 인증 처리 실패 시
     * @throws UserException 사용자 생성 실패 시
     * @throws CreditException 크레딧 지급 실패 시
     */
    @PostMapping("/signup/oauth")
    public LoginResponse signupWithOAuth(@RequestBody @Valid SignupRequest request)
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

    /**
     * 로그아웃을 수행합니다.
     *
     * @param request 로그아웃 요청 정보 (refreshToken)
     * @return 성공 응답
     * @throws AuthException 리프레시 토큰 삭제 실패 시
     */
    @PostMapping("/logout")
    public SuccessOnlyResponse logout(@RequestBody LogoutRequest request) throws AuthException {
        facade.logout(BearerAuthorizationUtils.removePrefix(request.refreshToken()));
        return SuccessOnlyResponse.create();
    }

    /**
     * 회원 탈퇴를 수행합니다.
     *
     * @param request 회원 탈퇴 요청 정보 (refreshToken)
     * @return 성공 응답
     * @throws AuthException 리프레시 토큰 삭제 실패 시
     * @throws UserException 사용자 삭제 실패 시
     */
    @DeleteMapping
    public SuccessOnlyResponse delete(@RequestBody LogoutRequest request) throws AuthException, UserException {
        facade.delete(BearerAuthorizationUtils.removePrefix(request.refreshToken()));
        return SuccessOnlyResponse.create();
    }
}
