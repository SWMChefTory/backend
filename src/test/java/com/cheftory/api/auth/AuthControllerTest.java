package com.cheftory.api.auth;

import static com.cheftory.api.utils.RestDocsUtils.getNestedClassPath;
import static com.cheftory.api.utils.RestDocsUtils.requestAccessTokenFields;
import static com.cheftory.api.utils.RestDocsUtils.requestPreprocessor;
import static com.cheftory.api.utils.RestDocsUtils.responseErrorFields;
import static com.cheftory.api.utils.RestDocsUtils.responsePreprocessor;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.cheftory.api.auth.dto.TokenReissueRequest;
import com.cheftory.api.auth.entity.AuthTokenType;
import com.cheftory.api.auth.exception.AuthErrorCode;
import com.cheftory.api.auth.exception.AuthException;
import com.cheftory.api.auth.model.AuthTokens;
import com.cheftory.api.auth.util.BearerAuthorizationUtils;
import com.cheftory.api.exception.GlobalErrorCode;
import com.cheftory.api.exception.GlobalExceptionHandler;
import com.cheftory.api.utils.RestDocsTest;
import com.cheftory.api.utils.RestDocsUtils;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.response.ValidatableMockMvcResponse;
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
    @DisplayName("사용자 ID 추출 (extractUserId)")
    class ExtractUserId {

        @Nested
        @DisplayName("Given - 유효한 토큰이 주어졌을 때")
        class GivenValidToken {
            UUID userId;
            String validToken;

            @BeforeEach
            void setUp() throws AuthException {
                userId = UUID.randomUUID();
                validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.example.token";

                doReturn(userId)
                        .when(authService)
                        .extractUserIdFromToken(validToken, com.cheftory.api.auth.entity.AuthTokenType.ACCESS);
            }

            @Nested
            @DisplayName("When - 추출을 요청하면")
            class WhenExtracting {
                ValidatableMockMvcResponse response;

                @BeforeEach
                void setUp() throws AuthException {
                    response = given().contentType(ContentType.JSON)
                            .header(HttpHeaders.AUTHORIZATION, BearerAuthorizationUtils.addPrefix(validToken))
                            .when()
                            .post("/papi/v1/auth/extract-user-id")
                            .then();
                }

                @Test
                @DisplayName("Then - 사용자 ID를 반환한다")
                void thenReturnsUserId() throws AuthException {
                    response.status(HttpStatus.OK)
                            .apply(document(
                                    getNestedClassPath(AuthControllerTest.this.getClass()) + "/{method-name}",
                                    requestPreprocessor(),
                                    responsePreprocessor(),
                                    requestAccessTokenFields(),
                                    responseFields(fieldWithPath("user_id").description("사용자 ID"))));
                    response.body("user_id", equalTo(userId.toString()));
                    verify(authService)
                            .extractUserIdFromToken(validToken, com.cheftory.api.auth.entity.AuthTokenType.ACCESS);
                }
            }
        }

        @Nested
        @DisplayName("Given - 유효하지 않은 토큰이 주어졌을 때")
        class GivenInvalidToken {
            String invalidToken;

            @BeforeEach
            void setUp() throws AuthException {
                invalidToken = "invalid.malformed.token";

                doThrow(new AuthException(AuthErrorCode.INVALID_TOKEN))
                        .when(authService)
                        .extractUserIdFromToken(invalidToken, com.cheftory.api.auth.entity.AuthTokenType.ACCESS);
            }

            @Nested
            @DisplayName("When - 추출을 요청하면")
            class WhenExtracting {
                ValidatableMockMvcResponse response;

                @BeforeEach
                void setUp() throws AuthException {
                    response = given().contentType(ContentType.JSON)
                            .header(HttpHeaders.AUTHORIZATION, BearerAuthorizationUtils.addPrefix(invalidToken))
                            .when()
                            .post("/papi/v1/auth/extract-user-id")
                            .then();
                }

                @Test
                @DisplayName("Then - 400 Bad Request를 반환한다")
                void thenReturnsBadRequest() throws AuthException {
                    response.status(HttpStatus.BAD_REQUEST)
                            .apply(document(
                                    getNestedClassPath(AuthControllerTest.this.getClass()) + "/{method-name}",
                                    requestPreprocessor(),
                                    responsePreprocessor(),
                                    requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION)
                                            .description("유효하지 않은 토큰")),
                                    responseFields(
                                            fieldWithPath("errorCode").description("에러 코드"),
                                            fieldWithPath("message").description("에러 메시지")),
                                    responseErrorFields(AuthErrorCode.INVALID_TOKEN)));
                    response.body("errorCode", equalTo(AuthErrorCode.INVALID_TOKEN.getErrorCode()));
                    verify(authService)
                            .extractUserIdFromToken(invalidToken, com.cheftory.api.auth.entity.AuthTokenType.ACCESS);
                }
            }
        }

        @Nested
        @DisplayName("Given - 헤더가 누락되었을 때")
        class GivenMissingHeader {

            @Nested
            @DisplayName("When - 추출을 요청하면")
            class WhenExtracting {
                ValidatableMockMvcResponse response;

                @BeforeEach
                void setUp() {
                    response = given().contentType(ContentType.JSON)
                            .when()
                            .post("/papi/v1/auth/extract-user-id")
                            .then();
                }

                @Test
                @DisplayName("Then - 400 Bad Request를 반환한다")
                void thenReturnsBadRequest() {
                    response.status(HttpStatus.BAD_REQUEST)
                            .apply(document(
                                    getNestedClassPath(AuthControllerTest.this.getClass()) + "/{method-name}",
                                    requestPreprocessor(),
                                    responsePreprocessor(),
                                    responseErrorFields(GlobalErrorCode.MISSING_HEADER)));
                    response.body("errorCode", equalTo(GlobalErrorCode.MISSING_HEADER.getErrorCode()));
                }
            }
        }

        @Nested
        @DisplayName("Given - 만료된 토큰일 때")
        class GivenExpiredToken {
            String expiredToken;

            @BeforeEach
            void setUp() throws AuthException {
                expiredToken = "expired.jwt.token";

                doThrow(new AuthException(AuthErrorCode.EXPIRED_TOKEN))
                        .when(authService)
                        .extractUserIdFromToken(expiredToken, AuthTokenType.ACCESS);
            }

            @Nested
            @DisplayName("When - 추출을 요청하면")
            class WhenExtracting {
                ValidatableMockMvcResponse response;

                @BeforeEach
                void setUp() throws AuthException {
                    response = given().contentType(ContentType.JSON)
                            .header(HttpHeaders.AUTHORIZATION, BearerAuthorizationUtils.addPrefix(expiredToken))
                            .when()
                            .post("/papi/v1/auth/extract-user-id")
                            .then();
                }

                @Test
                @DisplayName("Then - 400 Bad Request를 반환한다")
                void thenReturnsBadRequest() throws AuthException {
                    response.status(HttpStatus.BAD_REQUEST)
                            .apply(document(
                                    RestDocsUtils.getNestedClassPath(AuthControllerTest.this.getClass())
                                            + "/{method-name}",
                                    requestPreprocessor(),
                                    responsePreprocessor(),
                                    requestHeaders(headerWithName(HttpHeaders.AUTHORIZATION)
                                            .description("만료된 토큰")),
                                    responseErrorFields(AuthErrorCode.EXPIRED_TOKEN)));
                    response.body("errorCode", equalTo(AuthErrorCode.EXPIRED_TOKEN.getErrorCode()));
                    verify(authService)
                            .extractUserIdFromToken(expiredToken, com.cheftory.api.auth.entity.AuthTokenType.ACCESS);
                }
            }
        }
    }

    @Nested
    @DisplayName("토큰 재발급 (reissue)")
    class Reissue {

        @Nested
        @DisplayName("Given - 유효한 리프레시 토큰이 주어졌을 때")
        class GivenValidRefreshToken {
            String rawRefreshToken;
            String bearerToken;
            AuthTokens tokens;

            @BeforeEach
            void setUp() throws AuthException {
                rawRefreshToken = "valid-refresh-token";
                bearerToken = BearerAuthorizationUtils.addPrefix(rawRefreshToken);
                tokens = new AuthTokens("new-access-token", "new-refresh-token");

                doReturn(tokens).when(authService).reissue(rawRefreshToken);
            }

            @Nested
            @DisplayName("When - 재발급을 요청하면")
            class WhenReissuing {
                ValidatableMockMvcResponse response;

                @BeforeEach
                void setUp() {
                    var request = new TokenReissueRequest(bearerToken);
                    response = given().contentType(ContentType.JSON)
                            .body(request)
                            .when()
                            .post("/api/v1/auth/token/reissue")
                            .then();
                }

                @Test
                @DisplayName("Then - 새로운 토큰을 반환한다")
                void thenReturnsNewTokens() {
                    response.statusCode(HttpStatus.OK.value())
                            .apply(document(
                                    getNestedClassPath(AuthControllerTest.this.getClass()) + "/{method-name}",
                                    requestPreprocessor(),
                                    responsePreprocessor(),
                                    requestFields(fieldWithPath("refresh_token")
                                            .description("기존 리프레시 토큰 (Bearer prefix 포함)")),
                                    responseFields(
                                            fieldWithPath("access_token").description("재발급된 액세스 토큰"),
                                            fieldWithPath("refresh_token").description("재발급된 리프레시 토큰"))))
                            .body("access_token", equalTo("Bearer " + tokens.accessToken()))
                            .body("refresh_token", equalTo("Bearer " + tokens.refreshToken()));
                }
            }
        }

        @Nested
        @DisplayName("Given - 유효하지 않은 리프레시 토큰이 주어졌을 때")
        class GivenInvalidRefreshToken {
            String rawRefreshToken;
            String bearerToken;

            @BeforeEach
            void setUp() throws AuthException {
                rawRefreshToken = "invalid-refresh-token";
                bearerToken = "Bearer " + rawRefreshToken;

                doThrow(new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN))
                        .when(authService)
                        .reissue(rawRefreshToken);
            }

            @Nested
            @DisplayName("When - 재발급을 요청하면")
            class WhenReissuing {
                ValidatableMockMvcResponse response;

                @BeforeEach
                void setUp() {
                    var request = new TokenReissueRequest(bearerToken);
                    response = given().contentType(ContentType.JSON)
                            .body(request)
                            .when()
                            .post("/api/v1/auth/token/reissue")
                            .then();
                }

                @Test
                @DisplayName("Then - 400 Bad Request를 반환한다")
                void thenReturnsBadRequest() {
                    response.statusCode(HttpStatus.BAD_REQUEST.value())
                            .body("errorCode", equalTo(AuthErrorCode.INVALID_REFRESH_TOKEN.getErrorCode()));
                }
            }
        }
    }
}
