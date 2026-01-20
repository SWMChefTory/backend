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
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.cheftory.api.account.dto.LoginRequest;
import com.cheftory.api.account.dto.LogoutRequest;
import com.cheftory.api.account.model.Account;
import com.cheftory.api.auth.exception.AuthErrorCode;
import com.cheftory.api.auth.exception.AuthException;
import com.cheftory.api.exception.GlobalErrorCode;
import com.cheftory.api.exception.GlobalExceptionHandler;
import com.cheftory.api.user.entity.Gender;
import com.cheftory.api.user.entity.Provider;
import com.cheftory.api.user.entity.User;
import com.cheftory.api.user.exception.UserErrorCode;
import com.cheftory.api.user.exception.UserException;
import com.cheftory.api.utils.RestDocsTest;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@DisplayName("AccountController 테스트")
public class AccountControllerTest extends RestDocsTest {

    private AccountController controller;
    private AccountFacade accountFacade;
    private GlobalExceptionHandler globalExceptionHandler;

    private final LocalDate validDateOfBirth = LocalDate.of(2000, 1, 1);
    private final LocalDateTime validTermsOfUseAgreedAt = LocalDateTime.of(2023, 1, 1, 0, 0);
    private final LocalDateTime validPrivacyAgreedAt = LocalDateTime.of(2023, 1, 1, 0, 0);
    private final LocalDateTime validMarketingAgreedAt = LocalDateTime.of(2023, 1, 1, 0, 0);

    @BeforeEach
    void setUp() {
        accountFacade = mock(AccountFacade.class);
        controller = new AccountController(accountFacade);
        globalExceptionHandler = new GlobalExceptionHandler();
        mockMvc = mockMvcBuilder(controller).withAdvice(globalExceptionHandler).build();
    }

    @Nested
    @DisplayName("POST //api/v1/account/login/oauth - OAUTH 로그인")
    class LoginWithOAuth {

        @Nested
        @DisplayName("유효한 ID 토큰이 주어졌을 때")
        class ValidIdTokenScenario {

            private String validIdToken;
            private Provider provider;

            @BeforeEach
            void setUp() {
                validIdToken = "valid-id-token";
                provider = Provider.APPLE;

                User user = mock(User.class);
                doReturn("nickname").when(user).getNickname();
                doReturn(Gender.MALE).when(user).getGender();
                doReturn(validDateOfBirth).when(user).getDateOfBirth();
                doReturn(validTermsOfUseAgreedAt).when(user).getTermsOfUseAgreedAt();
                doReturn(validPrivacyAgreedAt).when(user).getPrivacyAgreedAt();
                doReturn(validMarketingAgreedAt).when(user).getMarketingAgreedAt();

                Account account = Account.of("access-token", "refresh-token", user);
                doReturn(account).when(accountFacade).login(validIdToken, provider);
            }

            @Test
            @DisplayName("성공 - 액세스 토큰, 리프레시 토큰, 사용자 정보를 반환한다")
            void shouldLoginWithOAuth() {
                var response = given().contentType(ContentType.JSON)
                        .body(new LoginRequest(validIdToken, provider))
                        .when()
                        .post("/api/v1/account/login/oauth")
                        .then()
                        .status(HttpStatus.OK)
                        .apply(document(
                                getNestedClassPath(this.getClass()) + "/{method-name}",
                                requestPreprocessor(),
                                responsePreprocessor(),
                                requestFields(
                                        fieldWithPath("id_token").description("OAUTH ID 토큰"),
                                        fieldWithPath("provider").description("OAUTH 제공자(GOOGLE, APPLE, KAKAO)")),
                                responseFields(
                                        fieldWithPath("access_token").description("액세스 토큰"),
                                        fieldWithPath("refresh_token").description("리프레시 토큰"),
                                        fieldWithPath("user_info.nickname").description("닉네임"),
                                        fieldWithPath("user_info.gender").description("성별"),
                                        fieldWithPath("user_info.date_of_birth").description("생년월일"),
                                        fieldWithPath("user_info.terms_of_use_agreed_at")
                                                .description("약관 동의 일시"),
                                        fieldWithPath("user_info.privacy_agreed_at")
                                                .description("개인정보 처리방침 동의 일시"),
                                        fieldWithPath("user_info.marketing_agreed_at")
                                                .description("마케팅 정보 수신 동의 일시"))));
                response.body("access_token", equalTo("Bearer access-token"))
                        .body("refresh_token", equalTo("Bearer refresh-token"))
                        .body("user_info.nickname", equalTo("nickname"))
                        .body("user_info.gender", equalTo(Gender.MALE.name()))
                        .body("user_info.date_of_birth", equalTo(validDateOfBirth.toString()));

                verify(accountFacade).login(validIdToken, provider);
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
                        .when(accountFacade)
                        .login(invalidIdToken, provider);
            }

            @Test
            @DisplayName("실패 - INVALID_ID_TOKEN 예외를 던지고, AUTH_006 코드를 반환한다")
            void shouldReturnBadRequestError() {
                var response = given().contentType(ContentType.JSON)
                        .body(new LoginRequest(invalidIdToken, provider))
                        .when()
                        .post("/api/v1/account/login/oauth")
                        .then()
                        .status(HttpStatus.BAD_REQUEST)
                        .apply(document(
                                getNestedClassPath(this.getClass()) + "/{method-name}",
                                requestPreprocessor(),
                                responsePreprocessor(),
                                responseErrorFields(AuthErrorCode.INVALID_ID_TOKEN)));
                response.body("errorCode", equalTo(AuthErrorCode.INVALID_ID_TOKEN.getErrorCode()));
            }
        }

        @Nested
        @DisplayName("존재하지 않는 유저일 때")
        class NonexistentUserScenario {

            private String idToken;
            private Provider provider;

            @BeforeEach
            void setUp() {
                idToken = "valid-but-unregistered-id-token";
                provider = Provider.GOOGLE;

                doThrow(new UserException(UserErrorCode.USER_NOT_FOUND))
                        .when(accountFacade)
                        .login(idToken, provider);
            }

            @Test
            @DisplayName("실패 - USER_NOT_FOUND 예외를 던지고, AUTH_004 코드를 반환한다")
            void shouldReturnUserNotFoundError() {
                var response = given().contentType(ContentType.JSON)
                        .body(new LoginRequest(idToken, provider))
                        .when()
                        .post("/api/v1/account/login/oauth")
                        .then()
                        .status(HttpStatus.BAD_REQUEST)
                        .apply(document(
                                getNestedClassPath(this.getClass()) + "/{method-name}",
                                requestPreprocessor(),
                                responsePreprocessor(),
                                responseErrorFields(UserErrorCode.USER_NOT_FOUND)));
                response.body("errorCode", equalTo(UserErrorCode.USER_NOT_FOUND.getErrorCode()));
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

            @BeforeEach
            void setUp() {
                validToken = "valid-id-token";
                provider = Provider.GOOGLE;
                nickname = "cheftory";
                gender = Gender.FEMALE;

                User user = mock(User.class);
                doReturn(nickname).when(user).getNickname();
                doReturn(gender).when(user).getGender();
                doReturn(validDateOfBirth).when(user).getDateOfBirth();
                doReturn(validTermsOfUseAgreedAt).when(user).getTermsOfUseAgreedAt();
                doReturn(validPrivacyAgreedAt).when(user).getPrivacyAgreedAt();
                doReturn(validMarketingAgreedAt).when(user).getMarketingAgreedAt();

                Account account = Account.of("access-token", "refresh-token", user);

                doReturn(account)
                        .when(accountFacade)
                        .signup(validToken, provider, nickname, gender, validDateOfBirth, true, true, true);
            }

            @Test
            @DisplayName("성공 - 액세스 토큰, 리프레시 토큰, 사용자 정보를 반환한다")
            void shouldSignupWithOAuth() {
                Map<String, Object> request = new HashMap<>();
                request.put("id_token", validToken);
                request.put("provider", Provider.GOOGLE.name());
                request.put("nickname", nickname);
                request.put("gender", gender);
                request.put("date_of_birth", validDateOfBirth.toString());
                request.put("is_terms_of_use_agreed", true);
                request.put("is_privacy_agreed", true);
                request.put("is_marketing_agreed", true);

                var response = given().contentType(ContentType.JSON)
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
                                        fieldWithPath("date_of_birth").description("생년월일"),
                                        fieldWithPath("is_terms_of_use_agreed").description("약관 동의 여부"),
                                        fieldWithPath("is_privacy_agreed").description("개인정보 처리방침 동의 여부"),
                                        fieldWithPath("is_marketing_agreed").description("마케팅 정보 수신 동의 여부")),
                                responseFields(
                                        fieldWithPath("access_token").description("액세스 토큰"),
                                        fieldWithPath("refresh_token").description("리프레시 토큰"),
                                        fieldWithPath("user_info.nickname").description("닉네임"),
                                        fieldWithPath("user_info.gender").description("성별"),
                                        fieldWithPath("user_info.date_of_birth").description("생년월일"),
                                        fieldWithPath("user_info.terms_of_use_agreed_at")
                                                .description("약관 동의 일시"),
                                        fieldWithPath("user_info.privacy_agreed_at")
                                                .description("개인정보 처리방침 동의 일시"),
                                        fieldWithPath("user_info.marketing_agreed_at")
                                                .description("마케팅 정보 수신 동의 일시"))));

                response.body("access_token", equalTo("Bearer access-token"))
                        .body("refresh_token", equalTo("Bearer refresh-token"))
                        .body("user_info.nickname", equalTo(nickname))
                        .body("user_info.gender", equalTo(gender.name()))
                        .body("user_info.date_of_birth", equalTo(validDateOfBirth.toString()));
            }
        }

        @Nested
        @DisplayName("유효하지 않은 회원가입 요청이 주어졌을 때")
        class InvalidRequestScenario {

            @Test
            @DisplayName("실패 - 닉네임이 null인 경우 BAD_REQUEST를 반환한다")
            void shouldFailWhenNicknameIsNull() {
                Map<String, Object> request = new HashMap<>();
                request.put("id_token", "valid-id-token");
                request.put("provider", Provider.GOOGLE.name());
                request.put("nickname", null);

                given().contentType(ContentType.JSON)
                        .body(request)
                        .when()
                        .post("/api/v1/account/signup/oauth")
                        .then()
                        .status(HttpStatus.BAD_REQUEST)
                        .apply(document(
                                getNestedClassPath(this.getClass()) + "/{method-name}",
                                requestPreprocessor(),
                                responsePreprocessor(),
                                responseErrorFields(GlobalErrorCode.FIELD_REQUIRED)));
            }
        }
    }

    @Nested
    @DisplayName("POST /api/v1/account/logout - 로그아웃")
    class Logout {

        private String refreshToken;

        @BeforeEach
        void setUp() {
            refreshToken = "Bearer refresh-token";
        }

        @Test
        @DisplayName("성공 - 로그아웃 처리")
        void shouldLogoutSuccessfully() {
            var request = new LogoutRequest(refreshToken);

            var response = given().contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .post("/api/v1/account/logout")
                    .then()
                    .status(HttpStatus.OK)
                    .apply(document(
                            getNestedClassPath(this.getClass()) + "/{method-name}",
                            requestPreprocessor(),
                            responsePreprocessor(),
                            requestFields(
                                    fieldWithPath("refresh_token").description("로그아웃할 리프레시 토큰 (Bearer prefix 포함)")),
                            responseFields(fieldWithPath("message").description("성공 메시지"))));

            assertSuccessResponse(response);
            verify(accountFacade).logout("refresh-token");
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/account/delete - 회원 탈퇴")
    class DeleteAccount {

        private String refreshToken;

        @BeforeEach
        void setUp() {
            refreshToken = "Bearer refresh-token";
        }

        @Test
        @DisplayName("성공 - 회원 탈퇴 처리")
        void shouldDeleteAccountSuccessfully() {
            var request = new LogoutRequest(refreshToken);

            var response = given().contentType(ContentType.JSON)
                    .body(request)
                    .when()
                    .delete("/api/v1/account")
                    .then()
                    .status(HttpStatus.OK)
                    .apply(document(
                            getNestedClassPath(this.getClass()) + "/{method-name}",
                            requestPreprocessor(),
                            responsePreprocessor(),
                            requestFields(
                                    fieldWithPath("refresh_token").description("회원 탈퇴 요청의 리프레시 토큰 (Bearer prefix 포함)")),
                            responseFields(fieldWithPath("message").description("성공 메시지"))));

            assertSuccessResponse(response);
            verify(accountFacade).delete("refresh-token");
        }
    }
}
