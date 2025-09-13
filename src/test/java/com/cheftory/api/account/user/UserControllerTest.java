package com.cheftory.api.account.user;

import static com.cheftory.api.utils.RestDocsUtils.getNestedClassPath;
import static com.cheftory.api.utils.RestDocsUtils.requestPreprocessor;
import static com.cheftory.api.utils.RestDocsUtils.responsePreprocessor;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.cheftory.api._common.security.UserArgumentResolver;
import com.cheftory.api.account.user.dto.UserMeRequest;
import com.cheftory.api.account.user.entity.Gender;
import com.cheftory.api.account.user.entity.Provider;
import com.cheftory.api.account.user.entity.UserStatus;
import com.cheftory.api.account.user.entity.User;
import com.cheftory.api.account.user.exception.UserErrorCode;
import com.cheftory.api.account.user.exception.UserException;
import com.cheftory.api.exception.GlobalExceptionHandler;
import com.cheftory.api.utils.RestDocsTest;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@DisplayName("UserController 테스트")
public class UserControllerTest extends RestDocsTest {

  private UserController controller;
  private UserService userService;
  private GlobalExceptionHandler globalExceptionHandler;

  private UUID fixedUserId;
  private LocalDate validDateOfBirth;
  private UserArgumentResolver userArgumentResolver;

  @BeforeEach
  void setUp() {
    fixedUserId = UUID.randomUUID();
    validDateOfBirth = LocalDate.of(2000, 1, 1);
    userService = mock(UserService.class);
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
  @DisplayName("GET /api/v1/users/me - 현재 로그인한 사용자 정보 조회")
  class getUserInfo {

    @Test
    @DisplayName("성공 - 유저 정보를 반환한다")
    void shouldReturnUserInfo() {

      User user = User.builder()
          .id(fixedUserId)
          .nickname("nickname")
          .gender(Gender.MALE)
          .dateOfBirth(validDateOfBirth)
          .userStatus(UserStatus.ACTIVE)
          .createdAt(LocalDateTime.now())
          .updatedAt(LocalDateTime.now())
          .termsOfUseAgreedAt(LocalDateTime.now())
          .privacyAgreedAt(LocalDateTime.now())
          .marketingAgreedAt(null)
          .provider(Provider.APPLE)
          .providerSub("apple-sub-123")
          .build();

      doReturn(user).when(userService).get(fixedUserId);

      var response = given()
          .contentType(ContentType.JSON)
          .attribute("userId", fixedUserId.toString())
          .header("Authorization", "Bearer accessToken")
          .when()
          .get("/api/v1/users/me")
          .then()
          .status(HttpStatus.OK)
          .apply(document(
              getNestedClassPath(this.getClass()) + "/{method-name}",
              requestPreprocessor(),
              responsePreprocessor(),
              responseFields(
                  fieldWithPath("nickname").description("닉네임"),
                  fieldWithPath("gender").description("성별"),
                  fieldWithPath("date_of_birth").description("생년월일"),
                  fieldWithPath("terms_of_use_agreed_at").description("이용약관 동의 일시"),
                  fieldWithPath("privacy_agreed_at").description("개인정보 처리방침 동의 일시"),
                  fieldWithPath("marketing_agreed_at").description("마케팅 정보 수신 동의 일시")
              )
          ));

      response.body("nickname", equalTo("nickname"))
          .body("gender", equalTo(Gender.MALE.name()))
          .body("date_of_birth", equalTo(validDateOfBirth.toString()));
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 유저일 경우 400를 반환한다")
    void shouldThrowUserNotFound_whenUserDoesNotExist() {

      doThrow(new UserException(UserErrorCode.USER_NOT_FOUND))
          .when(userService).get(fixedUserId);

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

    @Test
    @DisplayName("성공 - 닉네임만 수정한다")
    void shouldUpdateNicknameOnly() {
      // given
      doNothing().when(userService)
          .update(eq(fixedUserId), any(Optional.class), any(JsonNullable.class),
              any(JsonNullable.class));

      // when & then
      given()
          .contentType(ContentType.JSON)
          .body(Map.of("nickname", "newNickname")) // 닉네임만 보냄
          .attribute("userId", fixedUserId.toString())
          .header("Authorization", "Bearer accessToken")
          .when()
          .patch("/api/v1/users/me")
          .then()
          .status(HttpStatus.OK)
          .apply(document(
              getNestedClassPath(this.getClass()) + "/{method-name}",
              requestPreprocessor(),
              responsePreprocessor(),
              requestFields(
                  fieldWithPath("nickname").optional().description("변경할 닉네임")
              ),
              responseFields(
                  fieldWithPath("message").description("성공 메시지")
              )
          ));

      // 서비스 호출 인자 검증: nickname=Optional.of("newNickname"), 나머지는 absent
      verify(userService).update(
          eq(fixedUserId),
          eq(Optional.of("newNickname")),
          argThat(jn -> !jn.isPresent()),
          argThat(jn -> !jn.isPresent())
      );
    }

    @Test
    @DisplayName("성공 - 성별만 수정한다")
    void shouldUpdateGenderOnly() {
      // given
      doNothing().when(userService)
          .update(eq(fixedUserId), any(Optional.class), any(JsonNullable.class),
              any(JsonNullable.class));

      given()
          .contentType(ContentType.JSON)
          .body(Map.of("gender", Gender.MALE.name())) // 성별만 보냄
          .attribute("userId", fixedUserId.toString())
          .header("Authorization", "Bearer accessToken")
          .when()
          .patch("/api/v1/users/me")
          .then()
          .status(HttpStatus.OK)
          .apply(document(
              getNestedClassPath(this.getClass()) + "/{method-name}",
              requestPreprocessor(),
              responsePreprocessor(),
              requestFields(
                  fieldWithPath("gender").optional().description("성별 (MALE/FEMALE)")
              ),
              responseFields(
                  fieldWithPath("message").description("성공 메시지")
              )
          ));

      // gender 가 present 이고 값이 MALE 이어야 함, nickname/date_of_birth 는 미제공
      verify(userService).update(
          eq(fixedUserId),
          eq(Optional.empty()),
          argThat(jn -> jn.isPresent() && jn.get() == Gender.MALE),
          argThat(jn -> !jn.isPresent())
      );
    }

    @Test
    @DisplayName("성공 - 생년월일만 수정한다")
    void shouldUpdateBirthOnly() {
      // given
      doNothing().when(userService)
          .update(eq(fixedUserId), any(Optional.class), any(JsonNullable.class),
              any(JsonNullable.class));

      given()
          .contentType(ContentType.JSON)
          .body(Map.of("date_of_birth", validDateOfBirth.toString())) // 생년월일만 보냄 (yyyy-MM-dd)
          .attribute("userId", fixedUserId.toString())
          .header("Authorization", "Bearer accessToken")
          .when()
          .patch("/api/v1/users/me")
          .then()
          .status(HttpStatus.OK)
          .apply(document(
              getNestedClassPath(this.getClass()) + "/{method-name}",
              requestPreprocessor(),
              responsePreprocessor(),
              requestFields(
                  fieldWithPath("date_of_birth").optional().description("생년월일 (yyyy-MM-dd)")
              ),
              responseFields(
                  fieldWithPath("message").description("성공 메시지")
              )
          ));

      verify(userService).update(
          eq(fixedUserId),
          eq(Optional.empty()),
          argThat(jn -> !jn.isPresent()),
          argThat(jn -> jn.isPresent() && validDateOfBirth.equals(jn.get()))
      );
    }

    @Test
    @DisplayName("성공 - 성별을 null 로 비운다")
    void shouldClearGenderToNull() {
      doNothing().when(userService)
          .update(eq(fixedUserId), any(Optional.class), any(JsonNullable.class),
              any(JsonNullable.class));

      Map<String, Object> body = new HashMap<>();
      body.put("gender", null);

      // 명시적 null 전송
      given()
          .contentType(ContentType.JSON)
          .body(body)
          .attribute("userId", fixedUserId.toString())
          .header("Authorization", "Bearer accessToken")
          .when()
          .patch("/api/v1/users/me")
          .then()
          .status(HttpStatus.OK)
          .apply(document(
              getNestedClassPath(this.getClass()) + "/{method-name}",
              requestPreprocessor(),
              responsePreprocessor(),
              requestFields(
                  fieldWithPath("gender").optional().description("성별 (null 전송 시 값 제거)")
              ),
              responseFields(
                  fieldWithPath("message").description("성공 메시지")
              )
          ));

      verify(userService).update(
          eq(fixedUserId),
          eq(Optional.empty()),
          argThat(jn -> jn.isPresent() && jn.get() == null), // null 저장
          argThat(jn -> !jn.isPresent())
      );
    }

    @Test
    @DisplayName("성공 - 생년월일을 null 로 비운다")
    void shouldClearBirthToNull() {
      doNothing().when(userService)
          .update(eq(fixedUserId), any(Optional.class), any(JsonNullable.class),
              any(JsonNullable.class));

      Map<String, Object> body = new HashMap<>();
      body.put("date_of_birth", null);

      // 명시적 null 전송
      given()
          .contentType(ContentType.JSON)
          .body(body)
          .attribute("userId", fixedUserId.toString())
          .header("Authorization", "Bearer accessToken")
          .when()
          .patch("/api/v1/users/me")
          .then()
          .status(HttpStatus.OK)
          .apply(document(
              getNestedClassPath(this.getClass()) + "/{method-name}",
              requestPreprocessor(),
              responsePreprocessor(),
              requestFields(
                  fieldWithPath("date_of_birth").optional().description("생년월일 (null 전송 시 값 제거)")
              ),
              responseFields(
                  fieldWithPath("message").description("성공 메시지")
              )
          ));

      verify(userService).update(
          eq(fixedUserId),
          eq(Optional.empty()),
          argThat(jn -> !jn.isPresent()),
          argThat(jn -> jn.isPresent() && jn.get() == null) // null 저장
      );
    }
  }
}