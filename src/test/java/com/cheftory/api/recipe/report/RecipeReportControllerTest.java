package com.cheftory.api.recipe.report;

import static com.cheftory.api.utils.RestDocsUtils.getNestedClassPath;
import static com.cheftory.api.utils.RestDocsUtils.requestAccessTokenFields;
import static com.cheftory.api.utils.RestDocsUtils.requestPreprocessor;
import static com.cheftory.api.utils.RestDocsUtils.responseErrorFields;
import static com.cheftory.api.utils.RestDocsUtils.responsePreprocessor;
import static com.cheftory.api.utils.RestDocsUtils.responseSuccessFields;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;

import com.cheftory.api._common.security.UserArgumentResolver;
import com.cheftory.api.exception.GlobalExceptionHandler;
import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.report.entity.RecipeReportReason;
import com.cheftory.api.recipe.report.exception.RecipeReportErrorCode;
import com.cheftory.api.recipe.report.exception.RecipeReportException;
import com.cheftory.api.utils.RestDocsTest;
import io.restassured.http.ContentType;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@DisplayName("RecipeReportController 테스트")
public class RecipeReportControllerTest extends RestDocsTest {

    private RecipeReportController controller;
    private RecipeReportService service;
    private RecipeInfoService recipeInfoService;
    private GlobalExceptionHandler exceptionHandler;
    private UserArgumentResolver userArgumentResolver;

    @BeforeEach
    void setUp() {
        service = mock(RecipeReportService.class);
        recipeInfoService = mock(RecipeInfoService.class);
        exceptionHandler = new GlobalExceptionHandler();
        userArgumentResolver = new UserArgumentResolver();
        controller = new RecipeReportController(service);

        mockMvc = mockMvcBuilder(controller)
                .withValidator(RecipeInfoService.class, recipeInfoService)
                .withAdvice(exceptionHandler)
                .withArgumentResolver(userArgumentResolver)
                .build();
    }

    @Nested
    @DisplayName("레시피 신고 생성")
    class CreateRecipeReport {

        @Nested
        @DisplayName("Given - 레시피 신고 생성 요청이 주어졌을 때")
        class GivenValidParameters {

            private UUID recipeId;
            private UUID userId;
            private Map<String, Object> request;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                userId = UUID.randomUUID();

                var authentication = new UsernamePasswordAuthenticationToken(userId, null);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                request = Map.of(
                        "reason", "INAPPROPRIATE_CONTENT",
                        "description", "부적절한 콘텐츠가 포함되어 있습니다.");
            }

            @Nested
            @DisplayName("When - 레시피 신고를 생성한다면")
            class WhenCreatingRecipeReport {

                @BeforeEach
                void setUp() throws RecipeReportException {
                    doReturn(true).when(recipeInfoService).exists(any(UUID.class));
                    doNothing()
                            .when(service)
                            .report(any(UUID.class), any(UUID.class), any(RecipeReportReason.class), any(String.class));
                }

                @Test
                @DisplayName("Then - 레시피 신고를 생성한다 - 성공")
                void thenShouldCreateRecipeReport() throws RecipeReportException {
                    var response = given().contentType(ContentType.JSON)
                            .attribute("userId", userId)
                            .header("Authorization", "Bearer accessToken")
                            .body(request)
                            .when()
                            .post("/api/v1/recipes/{recipeId}/reports", recipeId)
                            .then()
                            .status(HttpStatus.OK)
                            .apply(document(
                                    getNestedClassPath(this.getClass()) + "/{method-name}",
                                    requestPreprocessor(),
                                    responsePreprocessor(),
                                    requestAccessTokenFields(),
                                    pathParameters(parameterWithName("recipeId").description("레시피 ID")),
                                    requestFields(
                                            fieldWithPath("reason")
                                                    .description(
                                                            "신고 사유 (INAPPROPRIATE_CONTENT, MISINFORMATION, LOW_QUALITY, OTHER)"),
                                            fieldWithPath("description").description("상세 설명 (최대 500자)")),
                                    responseSuccessFields()));
                    assertSuccessResponse(response);
                    verify(service)
                            .report(userId, recipeId, RecipeReportReason.INAPPROPRIATE_CONTENT, "부적절한 콘텐츠가 포함되어 있습니다.");
                }
            }
        }

        @Nested
        @DisplayName("Given - 중복 신고 요청일 때")
        class GivenDuplicateReport {

            private UUID recipeId;
            private UUID userId;
            private Map<String, Object> request;

            @BeforeEach
            void setUp() throws RecipeReportException {
                recipeId = UUID.randomUUID();
                userId = UUID.randomUUID();

                var authentication = new UsernamePasswordAuthenticationToken(userId, null);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                request = Map.of(
                        "reason", "OTHER",
                        "description", "기타 사유입니다.");

                doReturn(true).when(recipeInfoService).exists(any(UUID.class));
                doThrow(new RecipeReportException(RecipeReportErrorCode.DUPLICATE_REPORT))
                        .when(service)
                        .report(any(UUID.class), any(UUID.class), any(RecipeReportReason.class), any(String.class));
            }

            @Test
            @DisplayName("Then - DUPLICATE_REPORT 예외를 반환한다")
            void thenShouldThrowException() {
                given().contentType(ContentType.JSON)
                        .attribute("userId", userId)
                        .header("Authorization", "Bearer accessToken")
                        .body(request)
                        .when()
                        .post("/api/v1/recipes/{recipeId}/reports", recipeId)
                        .then()
                        .status(HttpStatus.CONFLICT)
                        .apply(document(
                                getNestedClassPath(this.getClass()) + "/{method-name}",
                                requestPreprocessor(),
                                responsePreprocessor(),
                                requestAccessTokenFields(),
                                pathParameters(parameterWithName("recipeId").description("레시피 ID")),
                                requestFields(
                                        fieldWithPath("reason")
                                                .description(
                                                        "신고 사유 (INAPPROPRIATE_CONTENT, MISINFORMATION, LOW_QUALITY, OTHER)"),
                                        fieldWithPath("description").description("상세 설명 (최대 500자)")),
                                responseErrorFields(RecipeReportErrorCode.DUPLICATE_REPORT)));
            }
        }
    }
}
