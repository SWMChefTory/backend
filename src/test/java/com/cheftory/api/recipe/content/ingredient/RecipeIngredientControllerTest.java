package com.cheftory.api.recipe.content.ingredient;

import static com.cheftory.api.utils.RestDocsUtils.getNestedClassPath;
import static com.cheftory.api.utils.RestDocsUtils.requestPreprocessor;
import static com.cheftory.api.utils.RestDocsUtils.responsePreprocessor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;

import com.cheftory.api.exception.GlobalExceptionHandler;
import com.cheftory.api.recipe.content.ingredient.entity.RecipeIngredient;
import com.cheftory.api.utils.RestDocsTest;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.response.ValidatableMockMvcResponse;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@DisplayName("RecipeIngredientController 테스트")
public class RecipeIngredientControllerTest extends RestDocsTest {

    private RecipeIngredientService recipeIngredientService;
    private RecipeIngredientController controller;
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        recipeIngredientService = mock(RecipeIngredientService.class);
        controller = new RecipeIngredientController(recipeIngredientService);
        exceptionHandler = new GlobalExceptionHandler();
        mockMvc = mockMvcBuilder(controller).withAdvice(exceptionHandler).build();
    }

    @Nested
    @DisplayName("레시피 재료 조회 (getRecipeIngredients)")
    class GetRecipeIngredients {

        @Nested
        @DisplayName("Given - 유효한 레시피 ID가 주어졌을 때")
        class GivenValidRecipeId {
            UUID recipeId;
            List<RecipeIngredient> recipeIngredients;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                RecipeIngredient ingredient1 = mock(RecipeIngredient.class);
                RecipeIngredient ingredient2 = mock(RecipeIngredient.class);

                doReturn("토마토").when(ingredient1).getName();
                doReturn("개").when(ingredient1).getUnit();
                doReturn(2).when(ingredient1).getAmount();

                doReturn("양파").when(ingredient2).getName();
                doReturn("개").when(ingredient2).getUnit();
                doReturn(1).when(ingredient2).getAmount();

                recipeIngredients = List.of(ingredient1, ingredient2);
                doReturn(recipeIngredients).when(recipeIngredientService).gets(any(UUID.class));
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenRequesting {
                ValidatableMockMvcResponse response;

                @BeforeEach
                void setUp() {
                    response = given().contentType(ContentType.JSON)
                            .get("/papi/v1/recipes/{recipeId}/ingredients", recipeId)
                            .then();
                }

                @Test
                @DisplayName("Then - 레시피 재료 목록을 반환한다")
                void thenReturnsIngredients() {
                    response.status(HttpStatus.OK)
                            .body("ingredients", hasSize(recipeIngredients.size()))
                            .apply(document(
                                    getNestedClassPath(RecipeIngredientControllerTest.this.getClass())
                                            + "/{method-name}",
                                    requestPreprocessor(),
                                    responsePreprocessor(),
                                    pathParameters(parameterWithName("recipeId").description("조회할 레시피 ID")),
                                    responseFields(
                                            fieldWithPath("ingredients").description("레시피 재료 목록"),
                                            fieldWithPath("ingredients[].name").description("재료 이름"),
                                            fieldWithPath("ingredients[].unit").description("재료 단위"),
                                            fieldWithPath("ingredients[].amount")
                                                    .description("재료 양"))));

                    verify(recipeIngredientService).gets(recipeId);

                    var responseBody = response.extract().jsonPath();

                    assertThat(responseBody.getList("ingredients")).hasSize(2);
                    assertThat(responseBody.getString("ingredients[0].name")).isEqualTo("토마토");
                    assertThat(responseBody.getString("ingredients[0].unit")).isEqualTo("개");
                    assertThat(responseBody.getInt("ingredients[0].amount")).isEqualTo(2);
                    assertThat(responseBody.getString("ingredients[1].name")).isEqualTo("양파");
                    assertThat(responseBody.getString("ingredients[1].unit")).isEqualTo("개");
                    assertThat(responseBody.getInt("ingredients[1].amount")).isEqualTo(1);
                }
            }
        }

        @Nested
        @DisplayName("Given - 재료가 없는 레시피 ID가 주어졌을 때")
        class GivenNoIngredients {
            UUID recipeId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                doReturn(Collections.emptyList()).when(recipeIngredientService).gets(any(UUID.class));
            }

            @Nested
            @DisplayName("When - 조회를 요청하면")
            class WhenRequesting {
                ValidatableMockMvcResponse response;

                @BeforeEach
                void setUp() {
                    response = given().contentType(ContentType.JSON)
                            .get("/papi/v1/recipes/{recipeId}/ingredients", recipeId)
                            .then();
                }

                @Test
                @DisplayName("Then - 빈 목록을 반환한다")
                void thenReturnsEmptyList() {
                    response.status(HttpStatus.OK).body("ingredients", hasSize(0));

                    verify(recipeIngredientService).gets(recipeId);
                }
            }
        }
    }
}
