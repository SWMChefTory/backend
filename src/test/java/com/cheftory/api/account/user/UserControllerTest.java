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
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.cheftory.api._common.security.UserArgumentResolver;
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
          .termsAgreedAt(LocalDateTime.now())
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
                  fieldWithPath("date_of_birth").description("생년월일")
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
}