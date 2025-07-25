package com.cheftory.api.auth;


import static com.cheftory.api.utils.RestDocsUtils.getNestedClassPath;
import static com.cheftory.api.utils.RestDocsUtils.requestAccessTokenFields;
import static com.cheftory.api.utils.RestDocsUtils.requestPreprocessor;
import static com.cheftory.api.utils.RestDocsUtils.responseErrorFields;
import static com.cheftory.api.utils.RestDocsUtils.responsePreprocessor;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.cheftory.api.account.auth.AuthController;
import com.cheftory.api.account.auth.AuthService;
import com.cheftory.api.account.auth.exception.AuthErrorCode;
import com.cheftory.api.account.auth.exception.AuthException;
import com.cheftory.api.exception.GlobalErrorCode;
import com.cheftory.api.exception.GlobalExceptionHandler;
import com.cheftory.api.utils.RestDocsTest;
import com.cheftory.api.utils.RestDocsUtils;
import io.restassured.http.ContentType;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

@DisplayName("AuthController 테스트")
public class AuthControllerTest extends RestDocsTest {

  private AuthService authService;
  private AuthController controller;
  private GlobalExceptionHandler globalExceptionHandler;

  @BeforeEach
  void setUp() {
    authService = mock(AuthService.class);
    controller = new AuthController(authService);
    globalExceptionHandler = new GlobalExceptionHandler();
    mockMvc = mockMvcBuilder(controller).withAdvice(globalExceptionHandler).build();
  }

  @Nested
  @DisplayName("POST /papi/v1/auth/userId - 사용자 ID 추출")
  class ExtractUserId {

    @Nested
    @DisplayName("유효한 토큰이 주어졌을 때")
    class ValidTokenScenario {

      private UUID userId;
      private String validToken;

      @BeforeEach
      void setUp() {
        userId = generateUserId();
        validToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.example.token";

        doReturn(userId).when(authService).extractUserIdFromToken(validToken);
      }

      @Test
      @DisplayName("사용자 ID를 성공적으로 반환한다")
      void shouldReturnUserId() {
        var response = given()
            .contentType(ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION, validToken)
            .when()
            .post("/papi/v1/auth/extract-user-id")
            .then()
            .status(HttpStatus.OK)
            .apply(
                document(
                    getNestedClassPath(this.getClass()) + "/{method-name}",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    requestAccessTokenFields(),
                    responseFields(
                        fieldWithPath("user_id").description("사용자 ID")
                    )
                )
            );
        response.body("user_id", equalTo(userId.toString()));
        verify(authService).extractUserIdFromToken(validToken);
      }
    }

    @Nested
    @DisplayName("유효하지 않은 토큰이 주어졌을 때")
    class InvalidTokenScenario {

      private String invalidToken;

      @BeforeEach
      void setUp() {
        invalidToken = "Bearer invalid.malformed.token";

        when(authService.extractUserIdFromToken(invalidToken))
            .thenThrow(new AuthException(AuthErrorCode.INVALID_TOKEN));
      }

      @Test
      @DisplayName("400 Bad Request 에러를 반환한다")
      void shouldReturnBadRequestError() {
        var response = given()
            .contentType(ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION, invalidToken)
            .when()
            .post("/papi/v1/auth/extract-user-id")
            .then()
            .status(HttpStatus.BAD_REQUEST)
            .apply(
                document(
                    getNestedClassPath(this.getClass()) + "/{method-name}",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("유효하지 않은 Bearer 토큰")
                    ),
                    responseFields(fieldWithPath("errorCode").description("에러 코드"),
                        fieldWithPath("message").description("에러 메시지")
                    ),
                    responseErrorFields(AuthErrorCode.INVALID_TOKEN)
                )
            );
        response.body("errorCode", equalTo(AuthErrorCode.INVALID_TOKEN.getErrorCode()));
        verify(authService).extractUserIdFromToken(invalidToken);
      }
    }

    @Nested
    @DisplayName("헤더가 누락된 경우")
    class MissingHeaderScenario {

      @Test
      @DisplayName("400 Bad Request 에러를 반환한다")
      void shouldReturnBadRequestError() {
        var response = given()
            .contentType(ContentType.JSON)
            .when()
            .post("/papi/v1/auth/extract-user-id")
            .then()
            .status(HttpStatus.BAD_REQUEST)
            .apply(
                document(
                    RestDocsUtils.getNestedClassPath(this.getClass()) + "/{method-name}",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    responseErrorFields(GlobalErrorCode.MISSING_HEADER)
                )
            );
        response.body("errorCode", equalTo(GlobalErrorCode.MISSING_HEADER.getErrorCode()));
      }
    }

    @Nested
    @DisplayName("만료된 토큰인 경우")
    class ExpiredTokenScenario {

      private String expiredToken;

      @BeforeEach
      void setUp() {
        expiredToken = "Bearer expired.jwt.token";

        when(authService.extractUserIdFromToken(expiredToken)).thenThrow(
            new AuthException(AuthErrorCode.EXPIRED_TOKEN));
      }

      @Test
      @DisplayName("400 Bad Request 에러를 반환한다")
      void shouldReturnBadRequestError() {
        var response = given()
            .contentType(ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION, expiredToken)
            .when()
            .post("/papi/v1/auth/extract-user-id")
            .then()
            .status(HttpStatus.BAD_REQUEST)
            .apply(
                document(
                    RestDocsUtils.getNestedClassPath(this.getClass()) + "/{method-name}",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("만료된 Bearer 토큰")
                    ),
                    responseErrorFields(AuthErrorCode.EXPIRED_TOKEN)
                )
            );
        response.body("errorCode", equalTo(AuthErrorCode.EXPIRED_TOKEN.getErrorCode()));
        verify(authService).extractUserIdFromToken(expiredToken);
      }
    }

    @Nested
    @DisplayName("빈 토큰인 경우")
    class EmptyTokenScenario {

      private String emptyToken;

      @BeforeEach
      void setUp() {
        emptyToken = "";

        when(authService.extractUserIdFromToken(emptyToken)).thenThrow(
            new AuthException(AuthErrorCode.INVALID_TOKEN));
      }

      @Test
      @DisplayName("400 Bad Request 에러를 반환한다")
      void shouldReturnBadRequestError() {
        var response = given().contentType(ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION, emptyToken)
            .when()
            .post("/papi/v1/auth/extract-user-id")
            .then()
            .status(HttpStatus.BAD_REQUEST)
            .apply(
                document(
                    RestDocsUtils.getNestedClassPath(this.getClass()) + "/{method-name}",
                    requestPreprocessor(),
                    responsePreprocessor(),
                    requestHeaders(
                        headerWithName(HttpHeaders.AUTHORIZATION).description("빈 Authorization 헤더")
                    ),
                    responseErrorFields(AuthErrorCode.INVALID_TOKEN)
                )
            );
        response.body("errorCode", equalTo(AuthErrorCode.INVALID_TOKEN.getErrorCode()));
        verify(authService).extractUserIdFromToken(emptyToken);
      }
    }
  }
  private UUID generateUserId() {
    return UUID.randomUUID();
  }
}