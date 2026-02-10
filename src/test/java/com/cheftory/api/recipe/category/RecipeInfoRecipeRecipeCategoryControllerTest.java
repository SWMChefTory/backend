package com.cheftory.api.recipe.category;

import static com.cheftory.api.utils.RestDocsUtils.getNestedClassPath;
import static com.cheftory.api.utils.RestDocsUtils.requestAccessTokenFields;
import static com.cheftory.api.utils.RestDocsUtils.requestPreprocessor;
import static com.cheftory.api.utils.RestDocsUtils.responsePreprocessor;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import com.cheftory.api._common.security.UserArgumentResolver;
import com.cheftory.api.exception.GlobalExceptionHandler;
import com.cheftory.api.recipe.category.exception.RecipeCategoryException;
import com.cheftory.api.utils.RestDocsTest;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.response.ValidatableMockMvcResponse;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@DisplayName("RecipeCategoryController 테스트")
public class RecipeInfoRecipeRecipeCategoryControllerTest extends RestDocsTest {

    private RecipeCategoryController controller;
    private RecipeCategoryService recipeCategoryService;
    private GlobalExceptionHandler exceptionHandler;
    private UserArgumentResolver userArgumentResolver;

    @BeforeEach
    void setUp() {
        recipeCategoryService = mock(RecipeCategoryService.class);
        exceptionHandler = new GlobalExceptionHandler();
        userArgumentResolver = new UserArgumentResolver();
        controller = new RecipeCategoryController(recipeCategoryService);

        mockMvc = mockMvcBuilder(controller)
                .withValidator(RecipeCategoryService.class, recipeCategoryService)
                .withAdvice(exceptionHandler)
                .withArgumentResolver(userArgumentResolver)
                .build();
    }

    @Nested
    @DisplayName("레시피 카테고리 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 생성 요청이 주어졌을 때")
        class GivenValidRequest {
            String categoryName;
            UUID userId;
            Map<String, Object> request;
            UUID recipeCategoryId;

            @BeforeEach
            void setUp() throws RecipeCategoryException {
                categoryName = "한식";
                userId = UUID.randomUUID();
                var authentication = new UsernamePasswordAuthenticationToken(userId, null);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                request = Map.of("name", categoryName);
                recipeCategoryId = UUID.randomUUID();
                doReturn(recipeCategoryId).when(recipeCategoryService).create("한식", userId);
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {
                ValidatableMockMvcResponse response;

                @BeforeEach
                void setUp() {
                    response = given().contentType(ContentType.JSON)
                            .attribute("userId", userId.toString())
                            .header("Authorization", "Bearer accessToken")
                            .body(request)
                            .post("/api/v1/recipes/categories")
                            .then();
                }

                @Test
                @DisplayName("Then - 카테고리를 생성하고 ID를 반환한다")
                void thenCreatesAndReturnsId() throws RecipeCategoryException {
                    response.status(HttpStatus.OK)
                            .apply(document(
                                    getNestedClassPath(RecipeInfoRecipeRecipeCategoryControllerTest.this.getClass())
                                            + "/{method-name}",
                                    requestPreprocessor(),
                                    responsePreprocessor(),
                                    requestAccessTokenFields(),
                                    requestFields(fieldWithPath("name").description("레시피 카테고리 이름")),
                                    responseFields(
                                            fieldWithPath("recipe_category_id").description("레시피 카테고리 ID"))));

                    response.assertThat().body("recipe_category_id", equalTo(recipeCategoryId.toString()));

                    verify(recipeCategoryService).create("한식", userId);
                }
            }
        }
    }
}
