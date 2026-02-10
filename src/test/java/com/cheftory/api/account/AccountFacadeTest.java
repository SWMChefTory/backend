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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    @Nested
    @DisplayName("로그인 (login)")
    class Login {

        @Nested
        @DisplayName("Given - 유효한 ID 토큰과 제공자가 주어졌을 때")
        class GivenValidCredentials {

            @BeforeEach
            void setUp() throws AuthException, UserException {
                doReturn(providerSub).when(authService).extractProviderSubFromIdToken(idToken, provider);
                doReturn(user).when(userService).get(provider, providerSub);
                doReturn(authTokens).when(authService).createAuthToken(userId);
                doNothing().when(authService).saveLoginSession(userId, authTokens.refreshToken());
            }

            @Nested
            @DisplayName("When - 로그인을 요청하면")
            class WhenLoggingIn {
                Account result;

                @BeforeEach
                void setUp() throws AuthException, UserException, CreditException {
                    result = sut.login(idToken, provider);
                }

                @Test
                @DisplayName("Then - 계정 정보를 반환하고 세션을 저장한다")
                void thenReturnsAccount() throws AuthException, CreditException {
                    assertThat(result.getAccessToken()).isEqualTo("access-token");
                    assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
                    assertThat(result.getUser().getNickname()).isEqualTo(nickname);
                    assertThat(result.getUser().getGender()).isEqualTo(gender);
                    assertThat(result.getUser().getDateOfBirth()).isEqualTo(dob);

                    verify(authService).saveLoginSession(userId, "refresh-token");
                    verify(creditService, never()).grant(any(Credit.class));
                }
            }
        }
    }

    @Nested
    @DisplayName("회원가입 (signup)")
    class Signup {

        @Nested
        @DisplayName("Given - 유효한 가입 정보가 주어졌을 때")
        class GivenValidInfo {

            @BeforeEach
            void setUp() throws AuthException, UserException, CreditException {
                doReturn(providerSub).when(authService).extractProviderSubFromIdToken(idToken, provider);
                doReturn(user)
                        .when(userService)
                        .create(nickname, gender, dob, provider, providerSub, true, true, false);

                doReturn(authTokens).when(authService).createAuthToken(userId);
                doNothing().when(authService).saveLoginSession(userId, authTokens.refreshToken());
                doNothing().when(creditService).grant(any(Credit.class));
            }

            @Nested
            @DisplayName("When - 가입을 요청하면")
            class WhenSigningUp {
                Account result;

                @BeforeEach
                void setUp() throws AuthException, UserException, CreditException {
                    result = sut.signup(idToken, provider, nickname, gender, dob, true, true, false);
                }

                @Test
                @DisplayName("Then - 계정을 반환하고 가입 보너스를 지급한다")
                void thenReturnsAccountAndGrantsBonus() throws CreditException, AuthException {
                    assertThat(result.getAccessToken()).isEqualTo("access-token");
                    assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
                    assertThat(result.getUser().getNickname()).isEqualTo(nickname);
                    assertThat(result.getUser().getGender()).isEqualTo(gender);
                    assertThat(result.getUser().getDateOfBirth()).isEqualTo(dob);

                    verify(authService).saveLoginSession(userId, "refresh-token");
                    verify(creditService).grant(any(Credit.class));
                }
            }
        }
    }

    @Nested
    @DisplayName("로그아웃 (logout)")
    class Logout {

        @Nested
        @DisplayName("Given - 유효한 리프레시 토큰이 주어졌을 때")
        class GivenValidToken {

            @BeforeEach
            void setUp() throws AuthException {
                doReturn(userId).when(authService).extractUserIdFromToken("refresh-token", AuthTokenType.REFRESH);
                doNothing().when(authService).deleteRefreshToken(userId, "refresh-token");
            }

            @Nested
            @DisplayName("When - 로그아웃을 요청하면")
            class WhenLoggingOut {

                @BeforeEach
                void setUp() throws AuthException {
                    sut.logout("refresh-token");
                }

                @Test
                @DisplayName("Then - 리프레시 토큰을 삭제한다")
                void thenDeletesToken() throws AuthException, UserException {
                    verify(authService).deleteRefreshToken(userId, "refresh-token");
                    verify(userService, never()).delete(any());
                }
            }
        }
    }

    @Nested
    @DisplayName("회원 탈퇴 (delete)")
    class Delete {

        @Nested
        @DisplayName("Given - 유효한 리프레시 토큰이 주어졌을 때")
        class GivenValidToken {

            @BeforeEach
            void setUp() throws AuthException, UserException {
                doReturn(userId).when(authService).extractUserIdFromToken("refresh-token", AuthTokenType.REFRESH);
                doNothing().when(authService).deleteRefreshToken(userId, "refresh-token");
                doNothing().when(userService).delete(userId);
            }

            @Nested
            @DisplayName("When - 탈퇴를 요청하면")
            class WhenDeleting {

                @BeforeEach
                void setUp() throws AuthException, UserException {
                    sut.delete("refresh-token");
                }

                @Test
                @DisplayName("Then - 토큰과 유저 정보를 삭제한다")
                void thenDeletesUserAndToken() throws AuthException, UserException {
                    verify(authService).deleteRefreshToken(userId, "refresh-token");
                    verify(userService).delete(userId);
                }
            }
        }
    }
}
