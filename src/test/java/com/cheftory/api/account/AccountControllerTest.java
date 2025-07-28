package com.cheftory.api.account;

import static com.cheftory.api.utils.RestDocsUtils.getNestedClassPath;
import static com.cheftory.api.utils.RestDocsUtils.requestPreprocessor;
import static com.cheftory.api.utils.RestDocsUtils.responseErrorFields;
import static com.cheftory.api.utils.RestDocsUtils.responsePreprocessor;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.cheftory.api.account.auth.exception.AuthErrorCode;
import com.cheftory.api.account.auth.exception.AuthException;
import com.cheftory.api.account.dto.LoginRequest;
import com.cheftory.api.account.dto.SignupRequest;
import com.cheftory.api.account.model.LoginResult;
import com.cheftory.api.account.model.UserInfo;
import com.cheftory.api.exception.GlobalExceptionHandler;
import com.cheftory.api.account.user.entity.Gender;
import com.cheftory.api.account.user.entity.Provider;
import com.cheftory.api.utils.RestDocsTest;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@DisplayName("AccountController 테스트")
public class AccountControllerTest extends RestDocsTest {

  private AccountController controller;
  private AccountService accountService;
  private GlobalExceptionHandler globalExceptionHandler;

  private final UUID fixedUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private final LocalDate fixedDate = LocalDate.of(2000, 1, 1);

  @BeforeEach
  void setUp() {
    accountService = mock(AccountService.class);
    controller = new AccountController(accountService);
    globalExceptionHandler = new GlobalExceptionHandler();
    mockMvc = mockMvcBuilder(controller)
        .withAdvice(globalExceptionHandler)
        .build();
  }

  @Nested
  @DisplayName("POST //api/v1/account/signin/oauth - OAUTH 로그인")
  class LoginWithOAuth {

    @Nested
    @DisplayName("유효한 ID 토큰이 주어졌을 때")
    class ValidIdTokenScenario {

      private String validIdToken;
      private Provider provider;
      private LoginResult loginResult;

      @BeforeEach
      void setUp() {
        validIdToken = "valid-id-token";
        provider = Provider.APPLE;
        loginResult = new LoginResult(
            "access-token",
            "refresh-token",
            new UserInfo(
                fixedUserId,
                "email",
                "nickname",
                Gender.MALE,
                fixedDate
            )
        );

        doReturn(loginResult).when(accountService).loginWithOAuth(validIdToken, provider);
      }

      @Test
      @DisplayName("성공 - 액세스 토큰, 리프레시 토큰, 사용자 정보를 반환한다")
      void shouldLoginWithOAuth() {
        var response = given()
            .contentType(ContentType.JSON)
            .body(new LoginRequest(validIdToken, provider))
            .when()
            .post("/api/v1/account/signin/oauth")
            .then()
            .status(HttpStatus.OK)
            .apply(
                document(
                    getNestedClassPath(this.getClass()) + "/{method-name}",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    requestFields(
                        fieldWithPath("id_token").description("OAUTH ID 토큰"),
                        fieldWithPath("provider").description("OAUTH 제공자(GOOGLE, APPLE, KAKAO)")
                    ),
                    responseFields(
                        fieldWithPath("access_token").description("액세스 토큰"),
                        fieldWithPath("refresh_token").description("리프레시 토큰"),
                        fieldWithPath("user_info.id").description("사용자 ID"),
                        fieldWithPath("user_info.email").description("이메일"),
                        fieldWithPath("user_info.nickname").description("닉네임"),
                        fieldWithPath("user_info.gender").description("성별"),
                        fieldWithPath("user_info.date_of_birth").description("생년월일")
                    )
                )
            );
        response.body("access_token", equalTo("Bearer access-token"))
            .body("refresh_token", equalTo("Bearer refresh-token"))
            .body("user_info.id", equalTo(fixedUserId.toString()))
            .body("user_info.email", equalTo("email"))
            .body("user_info.nickname", equalTo("nickname"))
            .body("user_info.gender", equalTo(Gender.MALE.name()))
            .body("user_info.date_of_birth", equalTo(fixedDate.toString()));

        verify(accountService).loginWithOAuth(validIdToken, provider);
      }
    }

    @Nested
    @DisplayName("유효하지 않은 ID 토큰이 주어졌을 때")
    class InvalidIdTokenScenario {

      private String invalidIdToken;
      private Provider provider;

      @BeforeEach
      void setUp() {
        invalidIdToken = "invalid-id-token";
        provider = Provider.GOOGLE;

        doThrow(new AuthException(AuthErrorCode.INVALID_ID_TOKEN))
            .when(accountService)
            .loginWithOAuth(invalidIdToken, provider);
      }

      @Test
      @DisplayName("실패 - INVALID_ID_TOKEN 예외를 던지고, AUTH_006 코드를 반환한다")
      void shouldReturnBadRequestError() {
        var response = given()
            .contentType(ContentType.JSON)
            .body(new LoginRequest(invalidIdToken, provider))
            .when()
            .post("/api/v1/account/signin/oauth")
            .then()
            .status(HttpStatus.BAD_REQUEST)
            .apply(
                document(
                    getNestedClassPath(this.getClass()) + "/{method-name}",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    responseErrorFields(AuthErrorCode.INVALID_ID_TOKEN)
                )
            );
        response.body("errorCode", equalTo(AuthErrorCode.INVALID_ID_TOKEN.getErrorCode()));
      }
    }
  }

  @Nested
  @DisplayName("POST //api/v1/account/signup/oauth - OAUTH 회원가입")
  class SignupWithOAuth {

    @Nested
    @DisplayName("유효한 회원가입 요청이 주어졌을 때")
    class ValidRequestScenario {

      private String validToken;
      private Provider provider;
      private String nickname;
      private Gender gender;
      private LoginResult loginResult;

      @BeforeEach
      void setUp() {
        validToken = "valid-id-token";
        provider = Provider.GOOGLE;
        nickname = "cheftory";
        gender = Gender.FEMALE;

        loginResult = new LoginResult(
            "access-token",
            "refresh-token",
            new UserInfo(
                fixedUserId,
                "email@example.com",
                nickname,
                gender,
                fixedDate
            )
        );

        doReturn(loginResult).when(accountService)
            .signupWithOAuth(validToken, provider, nickname, gender, fixedDate);
      }

      @Test
      @DisplayName("성공 - 액세스 토큰, 리프레시 토큰, 사용자 정보를 반환한다")
      void shouldSignupWithOAuth() {
        var request = new SignupRequest(validToken, provider, nickname, gender, fixedDate);

        var response = given()
            .contentType(ContentType.JSON)
            .body(request)
            .when()
            .post("/api/v1/account/signup/oauth")
            .then()
            .status(HttpStatus.OK)
            .apply(document(
                getNestedClassPath(this.getClass()) + "/{method-name}",
                requestPreprocessor(),
                responsePreprocessor(),
                requestFields(
                    fieldWithPath("id_token").description("OAUTH ID 토큰"),
                    fieldWithPath("provider").description("OAUTH 제공자(GOOGLE, APPLE, KAKAO)"),
                    fieldWithPath("nickname").description("닉네임"),
                    fieldWithPath("gender").description("성별"),
                    fieldWithPath("birth_of_date").description("생년월일")
                ),
                responseFields(
                    fieldWithPath("access_token").description("액세스 토큰"),
                    fieldWithPath("refresh_token").description("리프레시 토큰"),
                    fieldWithPath("user_info.id").description("사용자 ID"),
                    fieldWithPath("user_info.email").description("이메일"),
                    fieldWithPath("user_info.nickname").description("닉네임"),
                    fieldWithPath("user_info.gender").description("성별"),
                    fieldWithPath("user_info.date_of_birth").description("생년월일")
                )
            ));

        response.body("access_token", equalTo("Bearer access-token"))
            .body("refresh_token", equalTo("Bearer refresh-token"))
            .body("user_info.email", equalTo("email@example.com"))
            .body("user_info.nickname", equalTo(nickname))
            .body("user_info.gender", equalTo(gender.name()))
            .body("user_info.date_of_birth", equalTo(fixedDate.toString()));
      }
    }
  }
}
