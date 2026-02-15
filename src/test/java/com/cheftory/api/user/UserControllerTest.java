package com.cheftory.api.user;

import static com.cheftory.api.utils.RestDocsUtils.getNestedClassPath;
import static com.cheftory.api.utils.RestDocsUtils.requestPreprocessor;
import static com.cheftory.api.utils.RestDocsUtils.responsePreprocessor;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.security.UserArgumentResolver;
import com.cheftory.api.credit.exception.CreditException;
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
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@DisplayName("UserController 테스트")
public class UserControllerTest extends RestDocsTest {

    private UserController controller;
    private UserService userService;
    private GlobalExceptionHandler globalExceptionHandler;
    private Clock clock;

    private UUID fixedUserId;
    private LocalDate validDateOfBirth;
    private UserArgumentResolver userArgumentResolver;

    @BeforeEach
    void setUp() {
        fixedUserId = UUID.randomUUID();
        validDateOfBirth = LocalDate.of(2000, 1, 1);
        userService = mock(UserService.class);
        clock = mock(Clock.class);
        doReturn(LocalDateTime.now()).when(clock).now();
        controller = new UserController(userService);
        globalExceptionHandler = new GlobalExceptionHandler();
        userArgumentResolver = new UserArgumentResolver();

        mockMvc = mockMvcBuilder(controller)
                .withAdvice(globalExceptionHandler)
                .withArgumentResolver(userArgumentResolver)
                .build();

        var authentication = new UsernamePasswordAuthenticationToken(fixedUserId, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    @DisplayName("내 정보 조회 (getMe)")
    class GetMe {

        @Nested
        @DisplayName("Given - 유효한 사용자일 때")
        class GivenValidUser {

            @BeforeEach
            void setUp() throws UserException {
                User user = User.create(
                        "nickname", Gender.MALE, validDateOfBirth, Provider.APPLE, "apple-sub-123", true, clock);
                doReturn(user).when(userService).get(fixedUserId);
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {

                @Test
                @DisplayName("Then - 유저 정보를 반환한다")
                void thenReturnsUserInfo() {
                    var response = given().contentType(ContentType.JSON)
                            .attribute("userId", fixedUserId.toString())
                            .header("Authorization", "Bearer accessToken")
                            .when()
                            .get("/api/v1/users/me")
                            .then()
                            .status(HttpStatus.OK)
                            .apply(document(
                                    getNestedClassPath(UserControllerTest.this.getClass()) + "/{method-name}",
                                    requestPreprocessor(),
                                    responsePreprocessor(),
                                    responseFields(
                                            fieldWithPath("nickname").description("닉네임"),
                                            fieldWithPath("gender").description("성별"),
                                            fieldWithPath("date_of_birth").description("생년월일"),
                                            fieldWithPath("terms_of_use_agreed_at")
                                                    .description("이용약관 동의 일시"),
                                            fieldWithPath("privacy_agreed_at").description("개인정보 처리방침 동의 일시"),
                                            fieldWithPath("marketing_agreed_at").description("마케팅 정보 수신 동의 일시"),
                                            fieldWithPath("tutorial_at").description("튜토리얼 완료 일시"),
                                            fieldWithPath("provider_sub").description("공급자 고유번호"))));

                    response.body("nickname", equalTo("nickname"))
                            .body("gender", equalTo(Gender.MALE.name()))
                            .body("date_of_birth", equalTo(validDateOfBirth.toString()));
                }
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 사용자일 때")
        class GivenNonExistingUser {

            @BeforeEach
            void setUp() throws UserException {
                doThrow(new UserException(UserErrorCode.USER_NOT_FOUND))
                        .when(userService)
                        .get(fixedUserId);
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenGetting {

                @Test
                @DisplayName("Then - 404 Not Found를 반환한다")
                void thenReturnsBadRequest() {
                    given().contentType(ContentType.JSON)
                            .attribute("userId", fixedUserId.toString())
                            .header("Authorization", "Bearer accessToken")
                            .when()
                            .get("/api/v1/users/me")
                            .then()
                            .status(HttpStatus.NOT_FOUND)
                            .body("message", equalTo(UserErrorCode.USER_NOT_FOUND.getMessage()))
                            .body("errorCode", equalTo(UserErrorCode.USER_NOT_FOUND.getErrorCode()));
                }
            }
        }
    }

    @Nested
    @DisplayName("내 정보 수정 (updateMe)")
    class UpdateMe {

        String oldNickname = "oldNick";
        Gender oldGender = Gender.FEMALE;
        LocalDate oldBirth = LocalDate.of(1999, 1, 1);

        String newNickname = "newNick";
        Gender newGender = Gender.MALE;
        LocalDate newBirth = LocalDate.of(2000, 1, 1);

        @Nested
        @DisplayName("Given - 닉네임 수정 요청일 때")
        class GivenNicknameUpdate {

            @BeforeEach
            void setUp() throws UserException {
                User user =
                        User.create(newNickname, oldGender, oldBirth, Provider.APPLE, "apple-sub-123", false, clock);
                doReturn(user).when(userService).update(fixedUserId, newNickname, oldGender, oldBirth);
            }

            @Nested
            @DisplayName("When - 수정을 요청하면")
            class WhenUpdating {

                @Test
                @DisplayName("Then - 닉네임이 수정된 정보를 반환한다")
                void thenReturnsUpdatedInfo() {
                    Map<String, Object> request = new HashMap<>();
                    request.put("nickname", newNickname);
                    request.put("gender", oldGender);
                    request.put("date_of_birth", oldBirth);

                    var response = given().contentType(ContentType.JSON)
                            .body(request)
                            .attribute("userId", fixedUserId.toString())
                            .header("Authorization", "Bearer accessToken")
                            .when()
                            .patch("/api/v1/users/me")
                            .then()
                            .status(HttpStatus.OK)
                            .apply(document(
                                    getNestedClassPath(UserControllerTest.this.getClass()) + "/{method-name}",
                                    requestPreprocessor(),
                                    responsePreprocessor(),
                                    requestFields(
                                            fieldWithPath("nickname").description("변경할 닉네임"),
                                            fieldWithPath("gender").description("기존 성별"),
                                            fieldWithPath("date_of_birth").description("기존 생년월일")),
                                    responseFields(
                                            fieldWithPath("nickname").description("닉네임"),
                                            fieldWithPath("gender").description("성별"),
                                            fieldWithPath("date_of_birth").description("생년월일"),
                                            fieldWithPath("terms_of_use_agreed_at")
                                                    .description("이용약관 동의 일시"),
                                            fieldWithPath("privacy_agreed_at").description("개인정보 처리방침 동의 일시"),
                                            fieldWithPath("marketing_agreed_at").description("마케팅 정보 수신 동의 일시"),
                                            fieldWithPath("tutorial_at").description("튜토리얼 완료 일시"),
                                            fieldWithPath("provider_sub").description("공급자 고유번호"))));

                    response.body("nickname", equalTo(newNickname))
                            .body("gender", equalTo(oldGender.name()))
                            .body("date_of_birth", equalTo(oldBirth.toString()));
                }
            }
        }

        @Nested
        @DisplayName("Given - 성별 수정 요청일 때")
        class GivenGenderUpdate {

            @BeforeEach
            void setUp() throws UserException {
                User user =
                        User.create(oldNickname, newGender, oldBirth, Provider.APPLE, "apple-sub-123", false, clock);
                doReturn(user).when(userService).update(fixedUserId, oldNickname, newGender, oldBirth);
            }

            @Nested
            @DisplayName("When - 수정을 요청하면")
            class WhenUpdating {

                @Test
                @DisplayName("Then - 성별이 수정된 정보를 반환한다")
                void thenReturnsUpdatedInfo() {
                    Map<String, Object> request = new HashMap<>();
                    request.put("nickname", oldNickname);
                    request.put("gender", newGender);
                    request.put("date_of_birth", oldBirth);

                    var response = given().contentType(ContentType.JSON)
                            .body(request)
                            .attribute("userId", fixedUserId.toString())
                            .header("Authorization", "Bearer accessToken")
                            .when()
                            .patch("/api/v1/users/me")
                            .then()
                            .status(HttpStatus.OK)
                            .apply(document(
                                    getNestedClassPath(UserControllerTest.this.getClass()) + "/{method-name}",
                                    requestPreprocessor(),
                                    responsePreprocessor(),
                                    requestFields(
                                            fieldWithPath("nickname").description("기존 닉네임"),
                                            fieldWithPath("gender").description("변경할 성별 (NULL 가능)"),
                                            fieldWithPath("date_of_birth").description("기존 생년월일")),
                                    responseFields(
                                            fieldWithPath("nickname").description("닉네임"),
                                            fieldWithPath("gender").description("성별"),
                                            fieldWithPath("date_of_birth").description("생년월일"),
                                            fieldWithPath("terms_of_use_agreed_at")
                                                    .description("이용약관 동의 일시"),
                                            fieldWithPath("privacy_agreed_at").description("개인정보 처리방침 동의 일시"),
                                            fieldWithPath("marketing_agreed_at").description("마케팅 정보 수신 동의 일시"),
                                            fieldWithPath("tutorial_at").description("튜토리얼 완료 일시"),
                                            fieldWithPath("provider_sub").description("공급자 고유번호"))));

                    response.body("nickname", equalTo(oldNickname))
                            .body("gender", equalTo(newGender.name()))
                            .body("date_of_birth", equalTo(oldBirth.toString()));
                }
            }
        }

        @Nested
        @DisplayName("Given - 성별 NULL 수정 요청일 때")
        class GivenGenderNullUpdate {

            @BeforeEach
            void setUp() throws UserException {
                User user = User.create(oldNickname, null, oldBirth, Provider.APPLE, "apple-sub-123", false, clock);
                doReturn(user).when(userService).update(fixedUserId, oldNickname, null, oldBirth);
            }

            @Nested
            @DisplayName("When - 수정을 요청하면")
            class WhenUpdating {

                @Test
                @DisplayName("Then - 성별이 NULL로 수정된 정보를 반환한다")
                void thenReturnsUpdatedInfo() {
                    Map<String, Object> request = new HashMap<>();
                    request.put("nickname", oldNickname);
                    request.put("gender", null);
                    request.put("date_of_birth", oldBirth);

                    var response = given().contentType(ContentType.JSON)
                            .body(request)
                            .attribute("userId", fixedUserId.toString())
                            .header("Authorization", "Bearer accessToken")
                            .when()
                            .patch("/api/v1/users/me")
                            .then()
                            .status(HttpStatus.OK)
                            .apply(document(
                                    getNestedClassPath(UserControllerTest.this.getClass()) + "/{method-name}",
                                    requestPreprocessor(),
                                    responsePreprocessor(),
                                    requestFields(
                                            fieldWithPath("nickname").description("기존 닉네임"),
                                            fieldWithPath("gender").description("변경할 성별"),
                                            fieldWithPath("date_of_birth").description("기존 생년월일")),
                                    responseFields(
                                            fieldWithPath("nickname").description("닉네임"),
                                            fieldWithPath("gender").description("성별"),
                                            fieldWithPath("date_of_birth").description("생년월일"),
                                            fieldWithPath("terms_of_use_agreed_at")
                                                    .description("이용약관 동의 일시"),
                                            fieldWithPath("privacy_agreed_at").description("개인정보 처리방침 동의 일시"),
                                            fieldWithPath("marketing_agreed_at").description("마케팅 정보 수신 동의 일시"),
                                            fieldWithPath("tutorial_at").description("튜토리얼 완료 일시"),
                                            fieldWithPath("provider_sub").description("공급자 고유번호"))));

                    response.body("nickname", equalTo(oldNickname))
                            .body("gender", equalTo(null))
                            .body("date_of_birth", equalTo(oldBirth.toString()));
                }
            }
        }

        @Nested
        @DisplayName("Given - 생년월일 수정 요청일 때")
        class GivenBirthUpdate {

            @BeforeEach
            void setUp() throws UserException {
                User user =
                        User.create(oldNickname, oldGender, newBirth, Provider.APPLE, "apple-sub-123", false, clock);
                doReturn(user).when(userService).update(fixedUserId, oldNickname, oldGender, newBirth);
            }

            @Nested
            @DisplayName("When - 수정을 요청하면")
            class WhenUpdating {

                @Test
                @DisplayName("Then - 생년월일이 수정된 정보를 반환한다")
                void thenReturnsUpdatedInfo() {
                    Map<String, Object> request = new HashMap<>();
                    request.put("nickname", oldNickname);
                    request.put("gender", oldGender);
                    request.put("date_of_birth", newBirth);

                    var response = given().contentType(ContentType.JSON)
                            .body(request)
                            .attribute("userId", fixedUserId.toString())
                            .header("Authorization", "Bearer accessToken")
                            .when()
                            .patch("/api/v1/users/me")
                            .then()
                            .status(HttpStatus.OK)
                            .apply(document(
                                    getNestedClassPath(UserControllerTest.this.getClass()) + "/{method-name}",
                                    requestPreprocessor(),
                                    responsePreprocessor(),
                                    requestFields(
                                            fieldWithPath("nickname").description("기존 닉네임"),
                                            fieldWithPath("gender").description("기존 성별"),
                                            fieldWithPath("date_of_birth").description("변경할 생년월일")),
                                    responseFields(
                                            fieldWithPath("nickname").description("닉네임"),
                                            fieldWithPath("gender").description("성별"),
                                            fieldWithPath("date_of_birth").description("생년월일"),
                                            fieldWithPath("terms_of_use_agreed_at")
                                                    .description("이용약관 동의 일시"),
                                            fieldWithPath("privacy_agreed_at").description("개인정보 처리방침 동의 일시"),
                                            fieldWithPath("marketing_agreed_at").description("마케팅 정보 수신 동의 일시"),
                                            fieldWithPath("tutorial_at").description("튜토리얼 완료 일시"),
                                            fieldWithPath("provider_sub").description("공급자 고유번호"))));

                    response.body("nickname", equalTo(oldNickname))
                            .body("gender", equalTo(oldGender.name()))
                            .body("date_of_birth", equalTo(newBirth.toString()));
                }
            }
        }

        @Nested
        @DisplayName("Given - 생년월일 NULL 수정 요청일 때")
        class GivenBirthNullUpdate {

            @BeforeEach
            void setUp() throws UserException {
                User user = User.create(oldNickname, oldGender, null, Provider.APPLE, "apple-sub-123", false, clock);
                doReturn(user).when(userService).update(fixedUserId, oldNickname, oldGender, null);
            }

            @Nested
            @DisplayName("When - 수정을 요청하면")
            class WhenUpdating {

                @Test
                @DisplayName("Then - 생년월일이 NULL로 수정된 정보를 반환한다")
                void thenReturnsUpdatedInfo() {
                    Map<String, Object> request = new HashMap<>();
                    request.put("nickname", oldNickname);
                    request.put("gender", oldGender);
                    request.put("date_of_birth", null);

                    var response = given().contentType(ContentType.JSON)
                            .body(request)
                            .attribute("userId", fixedUserId.toString())
                            .header("Authorization", "Bearer accessToken")
                            .when()
                            .patch("/api/v1/users/me")
                            .then()
                            .status(HttpStatus.OK)
                            .apply(document(
                                    getNestedClassPath(UserControllerTest.this.getClass()) + "/{method-name}",
                                    requestPreprocessor(),
                                    responsePreprocessor(),
                                    requestFields(
                                            fieldWithPath("nickname").description("기존 닉네임"),
                                            fieldWithPath("gender").description("기존 성별"),
                                            fieldWithPath("date_of_birth").description("변경할 생년월일 (NULL 가능)")),
                                    responseFields(
                                            fieldWithPath("nickname").description("닉네임"),
                                            fieldWithPath("gender").description("성별"),
                                            fieldWithPath("date_of_birth").description("생년월일"),
                                            fieldWithPath("terms_of_use_agreed_at")
                                                    .description("이용약관 동의 일시"),
                                            fieldWithPath("privacy_agreed_at").description("개인정보 처리방침 동의 일시"),
                                            fieldWithPath("marketing_agreed_at").description("마케팅 정보 수신 동의 일시"),
                                            fieldWithPath("tutorial_at").description("튜토리얼 완료 일시"),
                                            fieldWithPath("provider_sub").description("공급자 고유번호"))));

                    response.body("nickname", equalTo(oldNickname))
                            .body("gender", equalTo(oldGender.name()))
                            .body("date_of_birth", equalTo(null));
                }
            }
        }
    }

    @Nested
    @DisplayName("튜토리얼 완료 (tutorial)")
    class Tutorial {

        @Nested
        @DisplayName("Given - 유효한 요청일 때")
        class GivenValidRequest {

            @Nested
            @DisplayName("When - 완료를 요청하면")
            class WhenCompleting {

                @Test
                @DisplayName("Then - 성공 메시지를 반환한다")
                void thenReturnsSuccess() {
                    var response = given().contentType(ContentType.JSON)
                            .attribute("userId", fixedUserId.toString())
                            .header("Authorization", "Bearer accessToken")
                            .when()
                            .post("/api/v1/users/tutorial")
                            .then()
                            .status(HttpStatus.OK);

                    response.body("message", equalTo("success"));
                }
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 사용자일 때")
        class GivenNonExistingUser {

            @BeforeEach
            void setUp() throws UserException, CreditException {
                doThrow(new UserException(UserErrorCode.USER_NOT_FOUND))
                        .when(userService)
                        .tutorial(fixedUserId);
            }

            @Nested
            @DisplayName("When - 완료를 요청하면")
            class WhenCompleting {

                @Test
                @DisplayName("Then - USER_NOT_FOUND 에러를 반환한다")
                void thenReturnsError() {
                    given().contentType(ContentType.JSON)
                            .attribute("userId", fixedUserId.toString())
                            .header("Authorization", "Bearer accessToken")
                            .when()
                            .post("/api/v1/users/tutorial")
                            .then()
                            .status(HttpStatus.NOT_FOUND)
                            .body("message", equalTo(UserErrorCode.USER_NOT_FOUND.getMessage()))
                            .body("errorCode", equalTo(UserErrorCode.USER_NOT_FOUND.getErrorCode()));
                }
            }
        }

        @Nested
        @DisplayName("Given - 이미 완료된 사용자일 때")
        class GivenAlreadyCompleted {

            @BeforeEach
            void setUp() throws UserException, CreditException {
                doThrow(new UserException(UserErrorCode.TUTORIAL_ALREADY_FINISHED))
                        .when(userService)
                        .tutorial(fixedUserId);
            }

            @Nested
            @DisplayName("When - 완료를 요청하면")
            class WhenCompleting {

                @Test
                @DisplayName("Then - TUTORIAL_ALREADY_FINISHED 에러를 반환한다")
                void thenReturnsError() {
                    given().contentType(ContentType.JSON)
                            .attribute("userId", fixedUserId.toString())
                            .header("Authorization", "Bearer accessToken")
                            .when()
                            .post("/api/v1/users/tutorial")
                            .then()
                            .status(HttpStatus.CONFLICT)
                            .body("message", equalTo(UserErrorCode.TUTORIAL_ALREADY_FINISHED.getMessage()))
                            .body("errorCode", equalTo(UserErrorCode.TUTORIAL_ALREADY_FINISHED.getErrorCode()));
                }
            }
        }
    }
}
