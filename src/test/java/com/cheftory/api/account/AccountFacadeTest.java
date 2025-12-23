package com.cheftory.api.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.cheftory.api.account.model.Account;
import com.cheftory.api.auth.AuthService;
import com.cheftory.api.auth.model.AuthTokens;
import com.cheftory.api.user.UserService;
import com.cheftory.api.user.entity.Gender;
import com.cheftory.api.user.entity.Provider;
import com.cheftory.api.user.entity.User;
import com.cheftory.api._common.Clock;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountFacadeTest {

  @Mock private AuthService authService;
  @Mock private UserService userService;
  @InjectMocks private AccountFacade accountFacade;

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
    Clock clock = new Clock();
    user = User.create(nickname, gender, dob, provider, providerSub, true, clock);
    userId = user.getId();
  }


  @Test
  void loginWithOAuth_shouldReturnAccount() {
    // given
    doReturn(providerSub).when(authService).extractProviderSubFromIdToken(idToken, provider);
    doReturn(user).when(userService).getByProviderAndProviderSub(provider, providerSub);
    doReturn(authTokens).when(authService).createAuthToken(userId);
    doNothing().when(authService).saveLoginSession(userId, authTokens.refreshToken());

    // when
    Account result = accountFacade.login(idToken, provider);

    // then
    assertThat(result.getAccessToken()).isEqualTo("access-token");
    assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
    assertThat(result.getUser().getNickname()).isEqualTo(nickname);
    assertThat(result.getUser().getGender()).isEqualTo(gender);
    assertThat(result.getUser().getDateOfBirth()).isEqualTo(dob);
    verify(authService).saveLoginSession(userId, "refresh-token");
  }

  @Test
  void signup_shouldReturnAccount() {
    // given
    doReturn(providerSub).when(authService).extractProviderSubFromIdToken(idToken, provider);
    doReturn(user)
        .when(userService)
        .create(nickname, gender, dob, provider, providerSub, true, true, false);
    doReturn(authTokens).when(authService).createAuthToken(userId);
    doNothing().when(authService).saveLoginSession(userId, authTokens.refreshToken());

    // when
    Account result =
        accountFacade.signup(idToken, provider, nickname, gender, dob, true, true, false);

    // then
    assertThat(result.getAccessToken()).isEqualTo("access-token");
    assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
    assertThat(result.getUser().getNickname()).isEqualTo(nickname);
    assertThat(result.getUser().getGender()).isEqualTo(gender);
    assertThat(result.getUser().getDateOfBirth()).isEqualTo(dob);
    verify(authService).saveLoginSession(userId, "refresh-token");
  }

  @Test
  void logout_shouldCallAuthServiceToDeleteRefreshToken() {
    // given
    doReturn(userId).when(authService).extractUserIdFromToken("refresh-token");
    doNothing().when(authService).deleteRefreshToken(userId, "refresh-token");

    // when
    accountFacade.logout("refresh-token");

    // then
    verify(authService).deleteRefreshToken(userId, "refresh-token");
  }

  @Test
  void delete_shouldCallAuthAndUserServiceToDeleteUser() {
    // given
    doReturn(userId).when(authService).extractUserIdFromToken("refresh-token");
    doNothing().when(authService).deleteRefreshToken(userId, "refresh-token");
    doNothing().when(userService).deleteUser(userId);

    // when
    accountFacade.delete("refresh-token");

    // then
    verify(authService).deleteRefreshToken(userId, "refresh-token");
    verify(userService).deleteUser(userId);
  }
}
