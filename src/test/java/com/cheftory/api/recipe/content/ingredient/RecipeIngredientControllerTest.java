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
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@DisplayName("RecipeIngredientController")
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
    @DisplayName("레시피 재료 조회")
    class GetRecipeIngredients {

        @Nested
        @DisplayName("Given - 유효한 레시피 ID가 주어졌을 때")
        class GivenValidRecipeId {

            private UUID recipeId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 레시피 재료를 조회한다면")
            class WhenRequestingRecipeIngredients {

                private List<RecipeIngredient> recipeIngredients;
                private RecipeIngredient ingredient1;
                private RecipeIngredient ingredient2;

                @BeforeEach
                void setUp() {
                    ingredient1 = mock(RecipeIngredient.class);
                    ingredient2 = mock(RecipeIngredient.class);

                    doReturn("토마토").when(ingredient1).getName();
                    doReturn("개").when(ingredient1).getUnit();
                    doReturn(2).when(ingredient1).getAmount();

                    doReturn("양파").when(ingredient2).getName();
                    doReturn("개").when(ingredient2).getUnit();
                    doReturn(1).when(ingredient2).getAmount();

                    recipeIngredients = List.of(ingredient1, ingredient2);
                    doReturn(recipeIngredients).when(recipeIngredientService).gets(any(UUID.class));
                }

                @Test
                @DisplayName("Then - 레시피 재료를 성공적으로 반환해야 한다")
                void thenShouldReturnRecipeIngredients() {
                    var response = given().contentType(ContentType.JSON)
                            .get("/papi/v1/recipes/{recipeId}/ingredients", recipeId)
                            .then()
                            .status(HttpStatus.OK)
                            .body("ingredients", hasSize(recipeIngredients.size()))
                            .apply(document(
                                    getNestedClassPath(this.getClass()) + "/{method-name}",
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
        @DisplayName("Given - 단일 재료만 있는 레시피 ID가 주어졌을 때")
        class GivenRecipeIdWithSingleIngredient {

            private UUID recipeId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 레시피 재료를 조회한다면")
            class WhenRequestingRecipeIngredients {

                private List<RecipeIngredient> recipeIngredients;
                private RecipeIngredient singleIngredient;

                @BeforeEach
                void setUp() {
                    singleIngredient = mock(RecipeIngredient.class);

                    doReturn("계란").when(singleIngredient).getName();
                    doReturn("개").when(singleIngredient).getUnit();
                    doReturn(3).when(singleIngredient).getAmount();

                    recipeIngredients = List.of(singleIngredient);
                    doReturn(recipeIngredients).when(recipeIngredientService).gets(any(UUID.class));
                }

                @Test
                @DisplayName("Then - 단일 레시피 재료를 성공적으로 반환해야 한다")
                void thenShouldReturnSingleRecipeIngredient() {
                    var response = given().contentType(ContentType.JSON)
                            .get("/papi/v1/recipes/{recipeId}/ingredients", recipeId)
                            .then()
                            .status(HttpStatus.OK)
                            .body("ingredients", hasSize(1));

                    verify(recipeIngredientService).gets(recipeId);

                    var responseBody = response.extract().jsonPath();

                    assertThat(responseBody.getString("ingredients[0].name")).isEqualTo("계란");
                    assertThat(responseBody.getString("ingredients[0].unit")).isEqualTo("개");
                    assertThat(responseBody.getInt("ingredients[0].amount")).isEqualTo(3);
                }
            }
        }

        @Nested
        @DisplayName("Given - 재료가 없는 레시피 ID가 주어졌을 때")
        class GivenRecipeIdWithNoIngredients {

            private UUID recipeId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 레시피 재료를 조회한다면")
            class WhenRequestingRecipeIngredients {

                @BeforeEach
                void setUp() {
                    doReturn(Collections.emptyList())
                            .when(recipeIngredientService)
                            .gets(any(UUID.class));
                }

                @Test
                @DisplayName("Then - 빈 재료 목록을 반환해야 한다")
                void thenShouldReturnEmptyIngredients() {
                    given().contentType(ContentType.JSON)
                            .get("/papi/v1/recipes/{recipeId}/ingredients", recipeId)
                            .then()
                            .status(HttpStatus.OK)
                            .body("ingredients", hasSize(0));

                    verify(recipeIngredientService).gets(recipeId);
                }
            }
        }

        @Nested
        @DisplayName("Given - 복잡한 여러 재료가 있는 레시피 ID가 주어졌을 때")
        class GivenRecipeIdWithMultipleIngredients {

            private UUID recipeId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 레시피 재료를 조회한다면")
            class WhenRequestingRecipeIngredients {

                private List<RecipeIngredient> recipeIngredients;

                @BeforeEach
                void setUp() {
                    RecipeIngredient ingredient1 = mock(RecipeIngredient.class);
                    RecipeIngredient ingredient2 = mock(RecipeIngredient.class);
                    RecipeIngredient ingredient3 = mock(RecipeIngredient.class);
                    RecipeIngredient ingredient4 = mock(RecipeIngredient.class);

                    doReturn("돼지고기").when(ingredient1).getName();
                    doReturn("g").when(ingredient1).getUnit();
                    doReturn(200).when(ingredient1).getAmount();

                    doReturn("김치").when(ingredient2).getName();
                    doReturn("g").when(ingredient2).getUnit();
                    doReturn(150).when(ingredient2).getAmount();

                    doReturn("양파").when(ingredient3).getName();
                    doReturn("개").when(ingredient3).getUnit();
                    doReturn(1).when(ingredient3).getAmount();

                    doReturn("대파").when(ingredient4).getName();
                    doReturn("대").when(ingredient4).getUnit();
                    doReturn(2).when(ingredient4).getAmount();

                    recipeIngredients = List.of(ingredient1, ingredient2, ingredient3, ingredient4);
                    doReturn(recipeIngredients).when(recipeIngredientService).gets(any(UUID.class));
                }

                @Test
                @DisplayName("Then - 모든 재료를 성공적으로 반환해야 한다")
                void thenShouldReturnAllRecipeIngredients() {
                    var response = given().contentType(ContentType.JSON)
                            .get("/papi/v1/recipes/{recipeId}/ingredients", recipeId)
                            .then()
                            .status(HttpStatus.OK)
                            .body("ingredients", hasSize(4));

                    verify(recipeIngredientService).gets(recipeId);

                    var responseBody = response.extract().jsonPath();

                    assertThat(responseBody.getList("ingredients")).hasSize(4);

                    assertThat(responseBody.getString("ingredients[0].name")).isEqualTo("돼지고기");
                    assertThat(responseBody.getString("ingredients[0].unit")).isEqualTo("g");
                    assertThat(responseBody.getInt("ingredients[0].amount")).isEqualTo(200);

                    assertThat(responseBody.getString("ingredients[1].name")).isEqualTo("김치");
                    assertThat(responseBody.getString("ingredients[1].unit")).isEqualTo("g");
                    assertThat(responseBody.getInt("ingredients[1].amount")).isEqualTo(150);

                    assertThat(responseBody.getString("ingredients[2].name")).isEqualTo("양파");
                    assertThat(responseBody.getString("ingredients[2].unit")).isEqualTo("개");
                    assertThat(responseBody.getInt("ingredients[2].amount")).isEqualTo(1);

                    assertThat(responseBody.getString("ingredients[3].name")).isEqualTo("대파");
                    assertThat(responseBody.getString("ingredients[3].unit")).isEqualTo("대");
                    assertThat(responseBody.getInt("ingredients[3].amount")).isEqualTo(2);
                }
            }
        }
    }
}
