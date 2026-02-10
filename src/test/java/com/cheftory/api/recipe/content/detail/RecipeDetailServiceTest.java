package com.cheftory.api.recipe.content.detail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cheftory.api.recipe.content.detail.client.RecipeDetailClient;
import com.cheftory.api.recipe.content.detail.client.dto.ClientRecipeDetailResponse;
import com.cheftory.api.recipe.content.detail.entity.RecipeDetail;
import com.cheftory.api.recipe.exception.RecipeException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeDetailService 테스트")
class RecipeDetailServiceTest {

    private RecipeDetailClient recipeDetailClient;
    private RecipeDetailService recipeDetailService;

    @BeforeEach
    void setUp() {
        recipeDetailClient = mock(RecipeDetailClient.class);
        recipeDetailService = new RecipeDetailService(recipeDetailClient);
    }

    @Nested
    @DisplayName("레시피 상세 정보 조회 (getRecipeDetails)")
    class GetRecipeDetails {

        @Nested
        @DisplayName("Given - 유효한 비디오 ID와 파일 정보가 주어졌을 때")
        class GivenValidVideoIdAndFileInfo {

            private String videoId;
            private String fileUri;
            private String mimeType;
            private ClientRecipeDetailResponse clientResponse;
            private RecipeDetail expectedRecipeDetail;

            @BeforeEach
            void setUp() throws RecipeException {
                videoId = "sample-video-id";
                fileUri = "s3://bucket/file.mp4";
                mimeType = "video/mp4";
                clientResponse = mock(ClientRecipeDetailResponse.class);

                expectedRecipeDetail = RecipeDetail.of(
                        "맛있는 김치찌개 만들기",
                        List.of(
                                RecipeDetail.Ingredient.of("김치", 200, "g"),
                                RecipeDetail.Ingredient.of("돼지고기", 150, "g"),
                                RecipeDetail.Ingredient.of("두부", 1, "모")),
                        List.of("한식", "찌개", "김치"),
                        2,
                        30);

                when(recipeDetailClient.fetch(videoId, fileUri, mimeType)).thenReturn(clientResponse);
                when(clientResponse.toRecipeDetail()).thenReturn(expectedRecipeDetail);
            }

            @Nested
            @DisplayName("When - 레시피 상세 정보를 조회하면")
            class WhenGetRecipeDetails {

                private RecipeDetail result;

                @BeforeEach
                void setUp() throws RecipeException {
                    result = recipeDetailService.getRecipeDetails(videoId, fileUri, mimeType);
                }

                @Test
                @DisplayName("Then - 상세 정보가 정상적으로 반환된다")
                void thenReturnsRecipeDetail() {
                    assertThat(result).isNotNull();
                    assertThat(result).isEqualTo(expectedRecipeDetail);
                    assertThat(result.description()).isEqualTo("맛있는 김치찌개 만들기");
                    assertThat(result.ingredients()).hasSize(3);
                    assertThat(result.ingredients().getFirst().name()).isEqualTo("김치");
                    assertThat(result.ingredients().getFirst().amount()).isEqualTo(200);
                    assertThat(result.ingredients().getFirst().unit()).isEqualTo("g");
                    assertThat(result.tags()).containsExactly("한식", "찌개", "김치");
                    assertThat(result.servings()).isEqualTo(2);
                    assertThat(result.cookTime()).isEqualTo(30);
                }

                @Test
                @DisplayName("Then - 클라이언트가 호출된다")
                void thenClientIsCalled() throws RecipeException {
                    verify(recipeDetailClient).fetch(eq(videoId), eq(fileUri), eq(mimeType));
                    verify(clientResponse).toRecipeDetail();
                }
            }
        }

        @Nested
        @DisplayName("Given - 간단한 레시피 정보가 주어졌을 때")
        class GivenSimpleRecipeInfo {

            private String videoId;
            private String fileUri;
            private String mimeType;
            private ClientRecipeDetailResponse clientResponse;
            private RecipeDetail simpleRecipeDetail;

            @BeforeEach
            void setUp() throws RecipeException {
                videoId = "simple-recipe-id";
                fileUri = "s3://bucket/simple.mp4";
                mimeType = "video/mp4";
                clientResponse = mock(ClientRecipeDetailResponse.class);

                simpleRecipeDetail = RecipeDetail.of(
                        "간단한 계란찜",
                        List.of(RecipeDetail.Ingredient.of("계란", 3, "개"), RecipeDetail.Ingredient.of("물", 50, "ml")),
                        List.of("간식"),
                        1,
                        5);

                when(recipeDetailClient.fetch(videoId, fileUri, mimeType)).thenReturn(clientResponse);
                when(clientResponse.toRecipeDetail()).thenReturn(simpleRecipeDetail);
            }

            @Nested
            @DisplayName("When - 간단한 레시피를 조회하면")
            class WhenGetSimpleRecipe {

                private RecipeDetail result;

                @BeforeEach
                void setUp() throws RecipeException {
                    result = recipeDetailService.getRecipeDetails(videoId, fileUri, mimeType);
                }

                @Test
                @DisplayName("Then - 간단한 정보가 반환된다")
                void thenReturnsSimpleDetail() {
                    assertThat(result).isNotNull();
                    assertThat(result.description()).isEqualTo("간단한 계란찜");
                    assertThat(result.ingredients()).hasSize(2);
                    assertThat(result.tags()).hasSize(1);
                    assertThat(result.servings()).isEqualTo(1);
                    assertThat(result.cookTime()).isEqualTo(5);
                }

                @Test
                @DisplayName("Then - 클라이언트가 호출된다")
                void thenClientIsCalled() throws RecipeException {
                    verify(recipeDetailClient).fetch(eq(videoId), eq(fileUri), eq(mimeType));
                }
            }
        }

        @Nested
        @DisplayName("Given - 빈 재료 목록을 가진 레시피가 주어졌을 때")
        class GivenRecipeWithEmptyIngredients {

            private String videoId;
            private String fileUri;
            private String mimeType;
            private ClientRecipeDetailResponse clientResponse;
            private RecipeDetail emptyIngredientsRecipe;

            @BeforeEach
            void setUp() throws RecipeException {
                videoId = "empty-ingredients-id";
                fileUri = "s3://bucket/empty.mp4";
                mimeType = "video/mp4";
                clientResponse = mock(ClientRecipeDetailResponse.class);

                emptyIngredientsRecipe = RecipeDetail.of("재료 없는 레시피", List.of(), List.of("기타"), 1, 10);

                when(recipeDetailClient.fetch(videoId, fileUri, mimeType)).thenReturn(clientResponse);
                when(clientResponse.toRecipeDetail()).thenReturn(emptyIngredientsRecipe);
            }

            @Nested
            @DisplayName("When - 빈 재료 목록 레시피를 조회하면")
            class WhenGetEmptyIngredientsRecipe {

                private RecipeDetail result;

                @BeforeEach
                void setUp() throws RecipeException {
                    result = recipeDetailService.getRecipeDetails(videoId, fileUri, mimeType);
                }

                @Test
                @DisplayName("Then - 빈 재료 목록이 반환된다")
                void thenReturnsEmptyIngredients() {
                    assertThat(result).isNotNull();
                    assertThat(result.description()).isEqualTo("재료 없는 레시피");
                    assertThat(result.ingredients()).isEmpty();
                    assertThat(result.tags()).containsExactly("기타");
                    assertThat(result.servings()).isEqualTo(1);
                    assertThat(result.cookTime()).isEqualTo(10);
                }

                @Test
                @DisplayName("Then - 클라이언트가 호출된다")
                void thenClientIsCalled() throws RecipeException {
                    verify(recipeDetailClient).fetch(eq(videoId), eq(fileUri), eq(mimeType));
                }
            }
        }
    }

    @Nested
    @DisplayName("ClientRecipeDetailResponse DTO 변환 (toRecipeDetail)")
    class ToRecipeDetail {

        @Nested
        @DisplayName("Given - 정상적인 데이터가 주어졌을 때")
        class GivenValidData {

            private ClientRecipeDetailResponse clientResponse;

            @BeforeEach
            void setUp() {
                List<ClientRecipeDetailResponse.Ingredient> ingredients = List.of(
                        new ClientRecipeDetailResponse.Ingredient("김치", 200, "g"),
                        new ClientRecipeDetailResponse.Ingredient("돼지고기", 150, "g"),
                        new ClientRecipeDetailResponse.Ingredient("두부", 1, "모"));

                clientResponse =
                        new ClientRecipeDetailResponse("맛있는 김치찌개 만들기", ingredients, List.of("한식", "찌개", "김치"), 2, 30);
            }

            @Nested
            @DisplayName("When - toRecipeDetail을 호출하면")
            class WhenCallToRecipeDetail {

                private RecipeDetail result;

                @BeforeEach
                void setUp() {
                    result = clientResponse.toRecipeDetail();
                }

                @Test
                @DisplayName("Then - RecipeDetail로 정상 변환된다")
                void thenConvertsToRecipeDetail() {
                    assertThat(result).isNotNull();
                    assertThat(result.description()).isEqualTo("맛있는 김치찌개 만들기");
                    assertThat(result.ingredients()).hasSize(3);
                    assertThat(result.ingredients().get(0).name()).isEqualTo("김치");
                    assertThat(result.ingredients().get(0).amount()).isEqualTo(200);
                    assertThat(result.ingredients().get(0).unit()).isEqualTo("g");
                    assertThat(result.ingredients().get(1).name()).isEqualTo("돼지고기");
                    assertThat(result.ingredients().get(2).name()).isEqualTo("두부");
                    assertThat(result.tags()).containsExactly("한식", "찌개", "김치");
                    assertThat(result.servings()).isEqualTo(2);
                    assertThat(result.cookTime()).isEqualTo(30);
                }
            }
        }

        @Nested
        @DisplayName("Given - 빈 재료 목록이 주어졌을 때")
        class GivenEmptyIngredients {

            private ClientRecipeDetailResponse clientResponse;

            @BeforeEach
            void setUp() {
                clientResponse = new ClientRecipeDetailResponse("간단한 레시피", List.of(), List.of("간단"), 1, 5);
            }

            @Nested
            @DisplayName("When - toRecipeDetail을 호출하면")
            class WhenCallToRecipeDetail {

                private RecipeDetail result;

                @BeforeEach
                void setUp() {
                    result = clientResponse.toRecipeDetail();
                }

                @Test
                @DisplayName("Then - 빈 재료 목록을 가진 RecipeDetail로 변환된다")
                void thenConvertsWithEmptyIngredients() {
                    assertThat(result).isNotNull();
                    assertThat(result.description()).isEqualTo("간단한 레시피");
                    assertThat(result.ingredients()).isEmpty();
                    assertThat(result.tags()).containsExactly("간단");
                    assertThat(result.servings()).isEqualTo(1);
                    assertThat(result.cookTime()).isEqualTo(5);
                }
            }
        }

        @Nested
        @DisplayName("Given - null 재료가 주어졌을 때")
        class GivenNullIngredients {

            private ClientRecipeDetailResponse clientResponse;

            @BeforeEach
            void setUp() {
                clientResponse = new ClientRecipeDetailResponse("테스트 설명", null, List.of("테스트"), 1, 5);
            }

            @Test
            @DisplayName("When - toRecipeDetail을 호출하면 Then - NullPointerException이 발생한다")
            void whenCallToRecipeDetail_thenThrowsNullPointerException() {
                org.junit.jupiter.api.Assertions.assertThrows(
                        NullPointerException.class, () -> clientResponse.toRecipeDetail());
            }
        }

        @Nested
        @DisplayName("Given - null이 아닌 필드들만 주어졌을 때")
        class GivenPartialNullValues {

            private ClientRecipeDetailResponse clientResponse;

            @BeforeEach
            void setUp() {
                clientResponse = new ClientRecipeDetailResponse(null, List.of(), null, null, null);
            }

            @Nested
            @DisplayName("When - toRecipeDetail을 호출하면")
            class WhenCallToRecipeDetail {

                private RecipeDetail result;

                @BeforeEach
                void setUp() {
                    result = clientResponse.toRecipeDetail();
                }

                @Test
                @DisplayName("Then - null 값이 올바르게 처리된다")
                void thenHandlesPartialNullValues() {
                    assertThat(result).isNotNull();
                    assertThat(result.description()).isNull();
                    assertThat(result.ingredients()).isEmpty();
                    assertThat(result.tags()).isNull();
                    assertThat(result.servings()).isNull();
                    assertThat(result.cookTime()).isNull();
                }
            }
        }
    }
}
