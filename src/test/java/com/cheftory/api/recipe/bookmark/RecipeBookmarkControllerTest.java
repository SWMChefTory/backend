package com.cheftory.api.recipe.bookmark;

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
import com.cheftory.api.recipe.category.RecipeCategoryService;
import com.cheftory.api.recipe.category.exception.RecipeCategoryErrorCode;
import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
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

@DisplayName("RecipeViewStatusController Tests")
public class RecipeBookmarkControllerTest extends RestDocsTest {

    private RecipeBookmarkController controller;
    private RecipeBookmarkService recipeBookmarkService;
    private RecipeBookmarkFacade recipeBookmarkFacade;
    private RecipeInfoService recipeInfoService;
    private RecipeCategoryService recipeCategoryService;
    private GlobalExceptionHandler exceptionHandler;
    private UserArgumentResolver userArgumentResolver;

    @BeforeEach
    void setUp() {
        recipeBookmarkService = mock(RecipeBookmarkService.class);
        recipeBookmarkFacade = mock(RecipeBookmarkFacade.class);
        recipeCategoryService = mock(RecipeCategoryService.class);
        recipeInfoService = mock(RecipeInfoService.class);
        exceptionHandler = new GlobalExceptionHandler();
        userArgumentResolver = new UserArgumentResolver();
        controller = new RecipeBookmarkController(recipeBookmarkService, recipeBookmarkFacade);

        mockMvc = mockMvcBuilder(controller)
                .withValidator(RecipeCategoryService.class, recipeCategoryService)
                .withValidator(RecipeInfoService.class, recipeInfoService)
                .withAdvice(exceptionHandler)
                .withArgumentResolver(userArgumentResolver)
                .build();
    }

    @Nested
    @DisplayName("레시피 북마크 카테고리 업데이트")
    class UpdateRecipeBookmarkRecipeCategory {

        @Nested
        @DisplayName("Given - 레시피 북마크 카테고리 업데이트 요청이 주어졌을 때")
        class GivenValidParametersForUpdate {

            private UUID categoryId;
            private UUID recipeId;
            private UUID userId;
            private Map<String, Object> request;

            @BeforeEach
            void setUp() {
                categoryId = UUID.randomUUID();
                recipeId = UUID.randomUUID();
                userId = UUID.randomUUID();

                var authentication = new UsernamePasswordAuthenticationToken(userId, null);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                request = Map.of("category_id", categoryId);
            }

            @Nested
            @DisplayName("When - 레시피 북마크 카테고리를 업데이트한다면")
            class WhenUpdatingRecipeBookmarkRecipeCategory {

                @BeforeEach
                void setUp() {
                    doReturn(true).when(recipeCategoryService).exists(any(UUID.class));
                    doNothing()
                            .when(recipeBookmarkService)
                            .updateCategory(any(UUID.class), any(UUID.class), any(UUID.class));
                    doReturn(true).when(recipeInfoService).exists(any(UUID.class));
                }

                @Test
                @DisplayName("Then - 레시피 북마크 카테고리를 업데이트한다 - 성공")
                void thenShouldUpdateRecipeViewStatusCategory() {
                    var response = given().contentType(ContentType.JSON)
                            .attribute("userId", userId)
                            .header("Authorization", "Bearer accessToken")
                            .body(request)
                            .when()
                            .put("/api/v1/recipes/{recipeId}/categories", recipeId)
                            .then()
                            .status(HttpStatus.OK)
                            .apply(document(
                                    getNestedClassPath(this.getClass()) + "/{method-name}",
                                    requestPreprocessor(),
                                    responsePreprocessor(),
                                    requestAccessTokenFields(),
                                    pathParameters(parameterWithName("recipeId").description("레시피 ID")),
                                    requestFields(fieldWithPath("category_id").description("레시피 북마크 카테고리 ID")),
                                    responseSuccessFields()));
                    assertSuccessResponse(response);
                    verify(recipeBookmarkService).updateCategory(userId, recipeId, categoryId);
                }
            }
        }
    }

    @Nested
    @DisplayName("레시피 북마크 카테고리 업데이트 - 예외 처리")
    class UpdateRecipeBookmarkRecipeRecipeCategoryException {

        @Nested
        @DisplayName("Given - 존재하지 않는 카테고리 ID가 주어졌을 때")
        class GivenNonExistentRecipeCategoryId {

            private UUID recipeId;
            private UUID userId;
            private Map<String, Object> request;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                userId = UUID.randomUUID();

                var authentication = new UsernamePasswordAuthenticationToken(userId, null);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                request = Map.of("category_id", UUID.randomUUID());
                doReturn(false).when(recipeCategoryService).exists(any(UUID.class));
            }

            @Test
            @DisplayName("Then - 예외가 발생해야 한다")
            void thenShouldThrowException() {
                given().contentType(ContentType.JSON)
                        .attribute("userId", userId)
                        .header("Authorization", "Bearer accessToken")
                        .body(request)
                        .when()
                        .put("/api/v1/recipes/{recipeId}/categories", recipeId)
                        .then()
                        .status(HttpStatus.BAD_REQUEST)
                        .apply(document(
                                getNestedClassPath(this.getClass()) + "/{method-name}",
                                requestPreprocessor(),
                                responsePreprocessor(),
                                requestAccessTokenFields(),
                                pathParameters(parameterWithName("recipeId").description("레시피 ID")),
                                requestFields(fieldWithPath("category_id").description("레시피 북마크 카테고리 ID")),
                                responseErrorFields(RecipeCategoryErrorCode.RECIPE_CATEGORY_NOT_FOUND)));
            }
        }
    }

    @Nested
    @DisplayName("레시피 북마크 삭제")
    class DeleteRecipeBookmark {
        @Nested
        @DisplayName("Given - 레시피 북마크 삭제 요청이 주어졌을 때")
        class GivenValidParametersForDelete {

            private UUID recipeId;
            private UUID userId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                userId = UUID.randomUUID();

                var authentication = new UsernamePasswordAuthenticationToken(userId, null);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                doNothing().when(recipeBookmarkService).delete(any(UUID.class), any(UUID.class));
                doReturn(true).when(recipeInfoService).exists(any(UUID.class));
            }

            @Nested
            @DisplayName("When - 레시피 북마크를 삭제한다면")
            class WhenDeletingRecipeBookmark {

                @Test
                @DisplayName("Then - 레시피 북마크를 삭제한다 - 성공")
                void thenShouldDeleteRecipeViewStatus() {
                    given().contentType(ContentType.JSON)
                            .attribute("userId", userId)
                            .header("Authorization", "Bearer accessToken")
                            .when()
                            .delete("/api/v1/recipes/{recipeId}/bookmark", recipeId)
                            .then()
                            .status(HttpStatus.OK)
                            .apply(document(
                                    getNestedClassPath(this.getClass()) + "/{method-name}",
                                    requestPreprocessor(),
                                    responsePreprocessor(),
                                    requestAccessTokenFields(),
                                    pathParameters(parameterWithName("recipeId").description("레시피 ID")),
                                    responseSuccessFields()));
                    verify(recipeBookmarkService).delete(userId, recipeId);
                }
            }
        }
    }

    @Nested
    @DisplayName("레시피 북마크 생성")
    class CreateRecipeBookmark {

        @Nested
        @DisplayName("Given - 레시피 북마크 생성 요청이 주어졌을 때")
        class GivenValidParametersForCreate {

            private UUID recipeId;
            private UUID userId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                userId = UUID.randomUUID();

                var authentication = new UsernamePasswordAuthenticationToken(userId, null);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                doReturn(true).when(recipeInfoService).exists(any(UUID.class));
            }

            @Nested
            @DisplayName("When - 레시피 북마크를 생성한다면")
            class WhenCreatingRecipeBookmark {

                @Test
                @DisplayName("Then - 레시피 북마크를 생성한다 - 성공")
                void thenShouldCreateRecipeBookmark() {
                    var response = given().contentType(ContentType.JSON)
                            .attribute("userId", userId)
                            .header("Authorization", "Bearer accessToken")
                            .when()
                            .post("/api/v1/recipes/{recipeId}/bookmark", recipeId)
                            .then()
                            .status(HttpStatus.OK)
                            .apply(document(
                                    getNestedClassPath(this.getClass()) + "/{method-name}",
                                    requestPreprocessor(),
                                    responsePreprocessor(),
                                    requestAccessTokenFields(),
                                    pathParameters(parameterWithName("recipeId").description("레시피 ID")),
                                    responseSuccessFields()));
                    assertSuccessResponse(response);
                    verify(recipeBookmarkFacade).createAndCharge(userId, recipeId);
                }
            }
        }

        @Nested
        @DisplayName("Given - 존재하지 않는 레시피 ID가 주어졌을 때")
        class GivenNonExistentRecipeId {

            private UUID recipeId;
            private UUID userId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                userId = UUID.randomUUID();

                var authentication = new UsernamePasswordAuthenticationToken(userId, null);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                doReturn(true).when(recipeInfoService).exists(any(UUID.class));
                doThrow(new RecipeException(RecipeErrorCode.RECIPE_NOT_FOUND))
                        .when(recipeBookmarkFacade)
                        .createAndCharge(any(UUID.class), any(UUID.class));
            }

            @Test
            @DisplayName("Then - 예외가 발생해야 한다")
            void thenShouldThrowException() {
                given().contentType(ContentType.JSON)
                        .attribute("userId", userId)
                        .header("Authorization", "Bearer accessToken")
                        .when()
                        .post("/api/v1/recipes/{recipeId}/bookmark", recipeId)
                        .then()
                        .status(HttpStatus.BAD_REQUEST)
                        .apply(document(
                                getNestedClassPath(this.getClass()) + "/{method-name}",
                                requestPreprocessor(),
                                responsePreprocessor(),
                                requestAccessTokenFields(),
                                pathParameters(parameterWithName("recipeId").description("레시피 ID")),
                                responseErrorFields(RecipeErrorCode.RECIPE_NOT_FOUND)));
            }
        }
    }
}
