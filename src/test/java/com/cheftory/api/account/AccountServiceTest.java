package com.cheftory.api.account;

import com.cheftory.api.account.auth.AuthService;
import com.cheftory.api.account.auth.model.AuthTokens;
import com.cheftory.api.account.model.LoginResult;
import com.cheftory.api.account.user.UserService;
import com.cheftory.api.account.user.entity.Gender;
import com.cheftory.api.account.user.entity.Provider;
import com.cheftory.api.account.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

  @Mock private AuthService authService;
  @Mock private UserService userService;
  @InjectMocks private AccountService accountService;

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
    user = User.create(nickname, gender, dob, provider, providerSub);
    userId = user.getId();
  }

  @Test
  void loginWithOAuth_shouldReturnLoginResult() {
    // given
    doReturn(providerSub).when(authService).extractProviderSubFromIdToken(idToken, provider);
    doReturn(user).when(userService).getByProviderAndProviderSub(provider, providerSub);
    doReturn(authTokens).when(authService).createAuthToken(userId);
    doNothing().when(authService).saveLoginSession(userId, authTokens.refreshToken());

    // when
    LoginResult result = accountService.loginWithOAuth(idToken, provider);

    // then
    assertThat(result.accessToken()).isEqualTo("access-token");
    assertThat(result.refreshToken()).isEqualTo("refresh-token");
    assertThat(result.userInfo().nickname()).isEqualTo(nickname);
    assertThat(result.userInfo().gender()).isEqualTo(gender);
    assertThat(result.userInfo().dateOfBirth()).isEqualTo(dob);
    verify(authService).saveLoginSession(userId, "refresh-token");
  }

  @Test
  void signupWithOAuth_shouldReturnLoginResult() {
    // given
    doReturn(providerSub).when(authService).extractProviderSubFromIdToken(idToken, provider);
    doReturn(userId).when(userService).create(nickname, gender, dob, provider, providerSub);
    doReturn(authTokens).when(authService).createAuthToken(userId);
    doNothing().when(authService).saveLoginSession(userId, authTokens.refreshToken());

    // when
    LoginResult result = accountService.signupWithOAuth(idToken, provider, nickname, gender, dob);

    // then
    assertThat(result.accessToken()).isEqualTo("access-token");
    assertThat(result.refreshToken()).isEqualTo("refresh-token");
    assertThat(result.userInfo().nickname()).isEqualTo(nickname);
    assertThat(result.userInfo().gender()).isEqualTo(gender);
    assertThat(result.userInfo().dateOfBirth()).isEqualTo(dob);
    verify(authService).saveLoginSession(userId, "refresh-token");
  }

  @Test
  void logout_shouldCallAuthServiceToDeleteRefreshToken() {
    // given
    doReturn(userId).when(authService).extractUserIdFromToken("refresh-token");
    doNothing().when(authService).deleteRefreshToken(userId, "refresh-token");

    // when
    accountService.logout("refresh-token");

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
    accountService.delete("refresh-token");

    // then
    verify(authService).deleteRefreshToken(userId, "refresh-token");
    verify(userService).deleteUser(userId);
  }
}
