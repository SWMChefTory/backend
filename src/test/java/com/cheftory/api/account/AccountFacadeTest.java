package com.cheftory.api.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.Clock;
import com.cheftory.api.account.model.Account;
import com.cheftory.api.auth.AuthService;
import com.cheftory.api.auth.model.AuthTokens;
import com.cheftory.api.credit.CreditService;
import com.cheftory.api.credit.entity.Credit;
import com.cheftory.api.user.UserService;
import com.cheftory.api.user.entity.Gender;
import com.cheftory.api.user.entity.Provider;
import com.cheftory.api.user.entity.User;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AccountFacade 테스트")
class AccountFacadeTest {

    private AuthService authService;
    private UserService userService;
    private CreditService creditService;

    private AccountFacade sut;

    private final String idToken = "dummy_id_token";
    private final Provider provider = Provider.APPLE;
    private final String providerSub = "apple_12345";
    private final String nickname = "테스터";
    private final Gender gender = Gender.MALE;
    private final LocalDate dob = LocalDate.of(2000, 1, 1);
    private final AuthTokens authTokens = new AuthTokens("access-token", "refresh-token");

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        userService = mock(UserService.class);
        creditService = mock(CreditService.class);

        sut = new AccountFacade(authService, userService, creditService);

        Clock clock = new Clock();
        user = User.create(nickname, gender, dob, provider, providerSub, true, clock);
        userId = user.getId();
    }

    @Test
    @DisplayName("login: providerSub 추출 -> 유저 조회 -> 토큰 발급 -> 세션 저장 -> Account 반환")
    void login_shouldReturnAccount() {
        doReturn(providerSub).when(authService).extractProviderSubFromIdToken(idToken, provider);
        doReturn(user).when(userService).getByProviderAndProviderSub(provider, providerSub);
        doReturn(authTokens).when(authService).createAuthToken(userId);
        doNothing().when(authService).saveLoginSession(userId, authTokens.refreshToken());

        Account result = sut.login(idToken, provider);

        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(result.getUser().getNickname()).isEqualTo(nickname);
        assertThat(result.getUser().getGender()).isEqualTo(gender);
        assertThat(result.getUser().getDateOfBirth()).isEqualTo(dob);

        verify(authService).saveLoginSession(userId, "refresh-token");
        verify(creditService, never()).grant(any(Credit.class));
    }

    @Test
    @DisplayName("signup: 유저 생성 -> 토큰 발급/세션 저장 -> 가입 보너스 크레딧 지급 -> Account 반환")
    void signup_shouldReturnAccount_andGrantSignupBonus() {
        doReturn(providerSub).when(authService).extractProviderSubFromIdToken(idToken, provider);
        doReturn(user).when(userService).create(nickname, gender, dob, provider, providerSub, true, true, false);

        doReturn(authTokens).when(authService).createAuthToken(userId);
        doNothing().when(authService).saveLoginSession(userId, authTokens.refreshToken());
        doNothing().when(creditService).grant(any(Credit.class));

        Account result = sut.signup(idToken, provider, nickname, gender, dob, true, true, false);

        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(result.getUser().getNickname()).isEqualTo(nickname);
        assertThat(result.getUser().getGender()).isEqualTo(gender);
        assertThat(result.getUser().getDateOfBirth()).isEqualTo(dob);

        verify(authService).saveLoginSession(userId, "refresh-token");
        verify(creditService).grant(any(Credit.class));
    }

    @Test
    @DisplayName("logout: refreshToken에서 userId 추출 후 refreshToken 삭제")
    void logout_shouldDeleteRefreshToken() {
        doReturn(userId).when(authService).extractUserIdFromToken("refresh-token");
        doNothing().when(authService).deleteRefreshToken(userId, "refresh-token");

        sut.logout("refresh-token");

        verify(authService).deleteRefreshToken(userId, "refresh-token");
        verify(userService, never()).deleteUser(any());
    }

    @Test
    @DisplayName("delete: refreshToken 삭제 후 유저 삭제")
    void delete_shouldDeleteRefreshTokenAndUser() {
        doReturn(userId).when(authService).extractUserIdFromToken("refresh-token");
        doNothing().when(authService).deleteRefreshToken(userId, "refresh-token");
        doNothing().when(userService).deleteUser(userId);

        sut.delete("refresh-token");

        verify(authService).deleteRefreshToken(userId, "refresh-token");
        verify(userService).deleteUser(userId);
    }
}
