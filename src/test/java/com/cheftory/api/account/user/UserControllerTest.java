package com.cheftory.api.account.user;

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

import com.cheftory.api._common.security.UserArgumentResolver;
import com.cheftory.api.account.user.entity.Gender;
import com.cheftory.api.account.user.entity.Provider;
import com.cheftory.api.account.user.entity.User;
import com.cheftory.api.account.user.entity.UserStatus;
import com.cheftory.api.account.user.exception.UserErrorCode;
import com.cheftory.api.account.user.exception.UserException;
import com.cheftory.api._common.Clock;
import com.cheftory.api.exception.GlobalExceptionHandler;
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

    mockMvc =
        mockMvcBuilder(controller)
            .withAdvice(globalExceptionHandler)
            .withArgumentResolver(userArgumentResolver)
            .build();

    var authentication = new UsernamePasswordAuthenticationToken(fixedUserId, null);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  @Nested
  @DisplayName("GET /api/v1/users/me - 현재 로그인한 사용자 정보 조회")
  class getUserInfo {

    @Test
    @DisplayName("성공 - 유저 정보를 반환한다")
    void shouldReturnUserInfo() {

      User user =
          User.create("nickname", Gender.MALE, validDateOfBirth, Provider.APPLE, "apple-sub-123", true, clock);

      doReturn(user).when(userService).get(fixedUserId);

      var response =
          given()
              .contentType(ContentType.JSON)
              .attribute("userId", fixedUserId.toString())
              .header("Authorization", "Bearer accessToken")
              .when()
              .get("/api/v1/users/me")
              .then()
              .status(HttpStatus.OK)
              .apply(
                  document(
                      getNestedClassPath(this.getClass()) + "/{method-name}",
                      requestPreprocessor(),
                      responsePreprocessor(),
                      responseFields(
                          fieldWithPath("nickname").description("닉네임"),
                          fieldWithPath("gender").description("성별"),
                          fieldWithPath("date_of_birth").description("생년월일"),
                          fieldWithPath("terms_of_use_agreed_at").description("이용약관 동의 일시"),
                          fieldWithPath("privacy_agreed_at").description("개인정보 처리방침 동의 일시"),
                          fieldWithPath("marketing_agreed_at").description("마케팅 정보 수신 동의 일시"),
                          fieldWithPath("provider_sub").description("공급자 고유번호"))));

      response
          .body("nickname", equalTo("nickname"))
          .body("gender", equalTo(Gender.MALE.name()))
          .body("date_of_birth", equalTo(validDateOfBirth.toString()));
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 유저일 경우 400를 반환한다")
    void shouldThrowUserNotFound_whenUserDoesNotExist() {

      doThrow(new UserException(UserErrorCode.USER_NOT_FOUND)).when(userService).get(fixedUserId);

      given()
          .contentType(ContentType.JSON)
          .attribute("userId", fixedUserId.toString())
          .header("Authorization", "Bearer accessToken")
          .when()
          .get("/api/v1/users/me")
          .then()
          .status(HttpStatus.BAD_REQUEST)
          .body("message", equalTo(UserErrorCode.USER_NOT_FOUND.getMessage()))
          .body("errorCode", equalTo(UserErrorCode.USER_NOT_FOUND.getErrorCode()));
    }
  }

  @Nested
  @DisplayName("PATCH /api/v1/users/me - 현재 로그인한 사용자 정보 수정")
  class updateUserInfo {

    String oldNickname = "oldNick";
    Gender oldGender = Gender.FEMALE;
    LocalDate oldBirth = LocalDate.of(1999, 1, 1);

    String newNickname = "newNick";
    Gender newGender = Gender.MALE;
    LocalDate newBirth = LocalDate.of(2000, 1, 1);

    @Test
    @DisplayName("성공 - 닉네임 수정")
    void shouldUpdateNicknameOnly() {
      // given

      User user =
          User.create(newNickname, oldGender, oldBirth, Provider.APPLE, "apple-sub-123", false, clock);

      doReturn(user).when(userService).update(fixedUserId, newNickname, oldGender, oldBirth);

      Map<String, Object> request = new HashMap<>();
      request.put("nickname", newNickname);
      request.put("gender", oldGender);
      request.put("date_of_birth", oldBirth);

      // when & then
      var response =
          given()
              .contentType(ContentType.JSON)
              .body(request)
              .attribute("userId", fixedUserId.toString())
              .header("Authorization", "Bearer accessToken")
              .when()
              .patch("/api/v1/users/me")
              .then()
              .status(HttpStatus.OK)
              .apply(
                  document(
                      getNestedClassPath(this.getClass()) + "/{method-name}",
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
                          fieldWithPath("terms_of_use_agreed_at").description("이용약관 동의 일시"),
                          fieldWithPath("privacy_agreed_at").description("개인정보 처리방침 동의 일시"),
                          fieldWithPath("marketing_agreed_at").description("마케팅 정보 수신 동의 일시"),
                          fieldWithPath("provider_sub").description("공급자 고유번호"))));

      response
          .body("nickname", equalTo(newNickname))
          .body("gender", equalTo(oldGender.name()))
          .body("date_of_birth", equalTo(oldBirth.toString()));
    }

    @Test
    @DisplayName("성공 - 성별 수정")
    void shouldUpdateGenderOnly() {
      // given
      User user =
          User.create(oldNickname, newGender, oldBirth, Provider.APPLE, "apple-sub-123", false, clock);

      doReturn(user).when(userService).update(fixedUserId, oldNickname, newGender, oldBirth);

      Map<String, Object> request = new HashMap<>();
      request.put("nickname", oldNickname);
      request.put("gender", newGender);
      request.put("date_of_birth", oldBirth);

      // when & then
      var response =
          given()
              .contentType(ContentType.JSON)
              .body(request)
              .attribute("userId", fixedUserId.toString())
              .header("Authorization", "Bearer accessToken")
              .when()
              .patch("/api/v1/users/me")
              .then()
              .status(HttpStatus.OK)
              .apply(
                  document(
                      getNestedClassPath(this.getClass()) + "/{method-name}",
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
                          fieldWithPath("terms_of_use_agreed_at").description("이용약관 동의 일시"),
                          fieldWithPath("privacy_agreed_at").description("개인정보 처리방침 동의 일시"),
                          fieldWithPath("marketing_agreed_at").description("마케팅 정보 수신 동의 일시"),
                          fieldWithPath("provider_sub").description("공급자 고유번호"))));

      response
          .body("nickname", equalTo(oldNickname))
          .body("gender", equalTo(newGender.name()))
          .body("date_of_birth", equalTo(oldBirth.toString()));
    }

    @Test
    @DisplayName("성공 - 성별 수정(NULL)")
    void shouldUpdateGenderToNULL() {
      // given
      User user =
          User.create(oldNickname, null, oldBirth, Provider.APPLE, "apple-sub-123", false, clock);

      doReturn(user).when(userService).update(fixedUserId, oldNickname, null, oldBirth);

      Map<String, Object> request = new HashMap<>();
      request.put("nickname", oldNickname);
      request.put("gender", null);
      request.put("date_of_birth", oldBirth);

      // when & then
      var response =
          given()
              .contentType(ContentType.JSON)
              .body(request)
              .attribute("userId", fixedUserId.toString())
              .header("Authorization", "Bearer accessToken")
              .when()
              .patch("/api/v1/users/me")
              .then()
              .status(HttpStatus.OK)
              .apply(
                  document(
                      getNestedClassPath(this.getClass()) + "/{method-name}",
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
                          fieldWithPath("terms_of_use_agreed_at").description("이용약관 동의 일시"),
                          fieldWithPath("privacy_agreed_at").description("개인정보 처리방침 동의 일시"),
                          fieldWithPath("marketing_agreed_at").description("마케팅 정보 수신 동의 일시"),
                          fieldWithPath("provider_sub").description("공급자 고유번호"))));

      response
          .body("nickname", equalTo(oldNickname))
          .body("gender", equalTo(null))
          .body("date_of_birth", equalTo(oldBirth.toString()));
    }

    @Test
    @DisplayName("성공 - 생년월일 수정")
    void shouldUpdateBirthOnly() {
      // given
      User user =
          User.create(oldNickname, oldGender, newBirth, Provider.APPLE, "apple-sub-123", false, clock);

      doReturn(user).when(userService).update(fixedUserId, oldNickname, oldGender, newBirth);

      Map<String, Object> request = new HashMap<>();
      request.put("nickname", oldNickname);
      request.put("gender", oldGender);
      request.put("date_of_birth", newBirth);

      // when & then
      var response =
          given()
              .contentType(ContentType.JSON)
              .body(request)
              .attribute("userId", fixedUserId.toString())
              .header("Authorization", "Bearer accessToken")
              .when()
              .patch("/api/v1/users/me")
              .then()
              .status(HttpStatus.OK)
              .apply(
                  document(
                      getNestedClassPath(this.getClass()) + "/{method-name}",
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
                          fieldWithPath("terms_of_use_agreed_at").description("이용약관 동의 일시"),
                          fieldWithPath("privacy_agreed_at").description("개인정보 처리방침 동의 일시"),
                          fieldWithPath("marketing_agreed_at").description("마케팅 정보 수신 동의 일시"),
                          fieldWithPath("provider_sub").description("공급자 고유번호"))));

      response
          .body("nickname", equalTo(oldNickname))
          .body("gender", equalTo(oldGender.name()))
          .body("date_of_birth", equalTo(newBirth.toString()));
    }

    @Test
    @DisplayName("성공 - 생년월일 수정(NULL)")
    void shouldUpdateBirthToNULL() {
      // given
      User user =
          User.create(oldNickname, oldGender, null, Provider.APPLE, "apple-sub-123", false, clock);

      doReturn(user).when(userService).update(fixedUserId, oldNickname, oldGender, null);

      Map<String, Object> request = new HashMap<>();
      request.put("nickname", oldNickname);
      request.put("gender", oldGender);
      request.put("date_of_birth", null);

      // when & then
      var response =
          given()
              .contentType(ContentType.JSON)
              .body(request)
              .attribute("userId", fixedUserId.toString())
              .header("Authorization", "Bearer accessToken")
              .when()
              .patch("/api/v1/users/me")
              .then()
              .status(HttpStatus.OK)
              .apply(
                  document(
                      getNestedClassPath(this.getClass()) + "/{method-name}",
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
                          fieldWithPath("terms_of_use_agreed_at").description("이용약관 동의 일시"),
                          fieldWithPath("privacy_agreed_at").description("개인정보 처리방침 동의 일시"),
                          fieldWithPath("marketing_agreed_at").description("마케팅 정보 수신 동의 일시"),
                          fieldWithPath("provider_sub").description("공급자 고유번호"))));

      response
          .body("nickname", equalTo(oldNickname))
          .body("gender", equalTo(oldGender.name()))
          .body("date_of_birth", equalTo(null));
    }
  }
}
