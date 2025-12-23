package com.cheftory.api.recipe.content.detail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cheftory.api.recipe.content.caption.entity.RecipeCaption;
import com.cheftory.api.recipe.content.detail.RecipeDetailService;
import com.cheftory.api.recipe.content.detail.client.RecipeDetailClient;
import com.cheftory.api.recipe.content.detail.client.dto.ClientRecipeDetailResponse;
import com.cheftory.api.recipe.content.detail.entity.RecipeDetail;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeDetailService")
public class RecipeDetailServiceTest {

  private RecipeDetailClient recipeDetailClient;
  private RecipeDetailService recipeDetailService;

  @BeforeEach
  void setUp() {
    recipeDetailClient = mock(RecipeDetailClient.class);
    recipeDetailService = new RecipeDetailService(recipeDetailClient);
  }

  @DisplayName("레시피 상세 정보 조회")
  @Nested
  class GetRecipeDetails {

    @Nested
    @DisplayName("Given - 유효한 비디오 ID와 자막이 주어졌을 때")
    class GivenValidVideoIdAndCaption {
      private String videoId;
      private RecipeCaption recipeCaption;
      private ClientRecipeDetailResponse clientResponse;
      private RecipeDetail expectedRecipeDetail;

      @BeforeEach
      void setUp() {
        videoId = "sample-video-id";
        recipeCaption = mock(RecipeCaption.class);
        clientResponse = mock(ClientRecipeDetailResponse.class);

        expectedRecipeDetail =
            RecipeDetail.of(
                "맛있는 김치찌개 만들기",
                List.of(
                    RecipeDetail.Ingredient.of("김치", 200, "g"),
                    RecipeDetail.Ingredient.of("돼지고기", 150, "g"),
                    RecipeDetail.Ingredient.of("두부", 1, "모")),
                List.of("한식", "찌개", "김치"),
                2,
                30);

        when(recipeDetailClient.fetchRecipeDetails(videoId, recipeCaption))
            .thenReturn(clientResponse);
        when(clientResponse.toRecipeDetail()).thenReturn(expectedRecipeDetail);
      }

      @DisplayName("When - 레시피 상세 정보를 조회하면")
      @Nested
      class WhenGetRecipeDetails {

        @Test
        @DisplayName("Then - 상세 정보가 정상적으로 반환된다")
        void thenReturnsRecipeDetail() {
          // when
          RecipeDetail result = recipeDetailService.getRecipeDetails(videoId, recipeCaption);

          // then
          assertThat(result).isNotNull();
          assertThat(result).isEqualTo(expectedRecipeDetail);
          assertThat(result.description()).isEqualTo("맛있는 김치찌개 만들기");
          assertThat(result.ingredients()).hasSize(3);
          assertThat(result.ingredients().get(0).name()).isEqualTo("김치");
          assertThat(result.ingredients().get(0).amount()).isEqualTo(200);
          assertThat(result.ingredients().get(0).unit()).isEqualTo("g");
          assertThat(result.tags()).containsExactly("한식", "찌개", "김치");
          assertThat(result.servings()).isEqualTo(2);
          assertThat(result.cookTime()).isEqualTo(30);

          verify(recipeDetailClient).fetchRecipeDetails(eq(videoId), eq(recipeCaption));
          verify(clientResponse).toRecipeDetail();
        }
      }
    }

    @Nested
    @DisplayName("Given - 간단한 레시피 정보가 주어졌을 때")
    class GivenSimpleRecipeInfo {
      private String videoId;
      private RecipeCaption recipeCaption;
      private ClientRecipeDetailResponse clientResponse;
      private RecipeDetail simpleRecipeDetail;

      @BeforeEach
      void setUp() {
        videoId = "simple-recipe-id";
        recipeCaption = mock(RecipeCaption.class);
        clientResponse = mock(ClientRecipeDetailResponse.class);

        simpleRecipeDetail =
            RecipeDetail.of(
                "간단한 계란찜",
                List.of(
                    RecipeDetail.Ingredient.of("계란", 3, "개"),
                    RecipeDetail.Ingredient.of("물", 50, "ml")),
                List.of("간식"),
                1,
                5);

        when(recipeDetailClient.fetchRecipeDetails(videoId, recipeCaption))
            .thenReturn(clientResponse);
        when(clientResponse.toRecipeDetail()).thenReturn(simpleRecipeDetail);
      }

      @Test
      @DisplayName("When - 간단한 레시피를 조회하면 Then - 간단한 정보가 반환된다")
      void whenGetSimpleRecipe_thenReturnsSimpleDetail() {
        // when
        RecipeDetail result = recipeDetailService.getRecipeDetails(videoId, recipeCaption);

        // then
        assertThat(result).isNotNull();
        assertThat(result.description()).isEqualTo("간단한 계란찜");
        assertThat(result.ingredients()).hasSize(2);
        assertThat(result.tags()).hasSize(1);
        assertThat(result.servings()).isEqualTo(1);
        assertThat(result.cookTime()).isEqualTo(5);

        verify(recipeDetailClient).fetchRecipeDetails(eq(videoId), eq(recipeCaption));
      }
    }

    @Nested
    @DisplayName("Given - 빈 재료 목록을 가진 레시피가 주어졌을 때")
    class GivenRecipeWithEmptyIngredients {
      private String videoId;
      private RecipeCaption recipeCaption;
      private ClientRecipeDetailResponse clientResponse;
      private RecipeDetail emptyIngredientsRecipe;

      @BeforeEach
      void setUp() {
        videoId = "empty-ingredients-id";
        recipeCaption = mock(RecipeCaption.class);
        clientResponse = mock(ClientRecipeDetailResponse.class);

        emptyIngredientsRecipe = RecipeDetail.of("재료 없는 레시피", List.of(), List.of("기타"), 1, 10);

        when(recipeDetailClient.fetchRecipeDetails(videoId, recipeCaption))
            .thenReturn(clientResponse);
        when(clientResponse.toRecipeDetail()).thenReturn(emptyIngredientsRecipe);
      }

      @Test
      @DisplayName("When - 빈 재료 목록 레시피를 조회하면 Then - 빈 재료 목록이 반환된다")
      void whenGetEmptyIngredientsRecipe_thenReturnsEmptyIngredients() {
        // when
        RecipeDetail result = recipeDetailService.getRecipeDetails(videoId, recipeCaption);

        // then
        assertThat(result).isNotNull();
        assertThat(result.description()).isEqualTo("재료 없는 레시피");
        assertThat(result.ingredients()).isEmpty();
        assertThat(result.tags()).containsExactly("기타");
        assertThat(result.servings()).isEqualTo(1);
        assertThat(result.cookTime()).isEqualTo(10);

        verify(recipeDetailClient).fetchRecipeDetails(eq(videoId), eq(recipeCaption));
      }
    }
  }

  @DisplayName("ClientRecipeDetailResponse DTO 변환 테스트")
  @Nested
  class ClientRecipeDetailResponseTest {

    @Test
    @DisplayName("ClientRecipeDetailResponse.toRecipeDetail() - 정상 변환")
    void shouldConvertToRecipeDetailCorrectly() {
      // given
      List<ClientRecipeDetailResponse.Ingredient> ingredients =
          List.of(
              new ClientRecipeDetailResponse.Ingredient("김치", 200, "g"),
              new ClientRecipeDetailResponse.Ingredient("돼지고기", 150, "g"),
              new ClientRecipeDetailResponse.Ingredient("두부", 1, "모"));

      ClientRecipeDetailResponse clientResponse =
          new ClientRecipeDetailResponse(
              "맛있는 김치찌개 만들기", ingredients, List.of("한식", "찌개", "김치"), 2, 30);

      // when
      RecipeDetail result = clientResponse.toRecipeDetail();

      // then
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

    @Test
    @DisplayName("ClientRecipeDetailResponse.toRecipeDetail() - 빈 재료 목록")
    void shouldConvertWithEmptyIngredients() {
      // given
      ClientRecipeDetailResponse clientResponse =
          new ClientRecipeDetailResponse("간단한 레시피", List.of(), List.of("간단"), 1, 5);

      // when
      RecipeDetail result = clientResponse.toRecipeDetail();

      // then
      assertThat(result).isNotNull();
      assertThat(result.description()).isEqualTo("간단한 레시피");
      assertThat(result.ingredients()).isEmpty();
      assertThat(result.tags()).containsExactly("간단");
      assertThat(result.servings()).isEqualTo(1);
      assertThat(result.cookTime()).isEqualTo(5);
    }

    @Test
    @DisplayName("ClientRecipeDetailResponse.toRecipeDetail() - null 재료 처리는 예외 발생")
    void shouldThrowExceptionWithNullIngredients() {
      // given
      ClientRecipeDetailResponse clientResponse =
          new ClientRecipeDetailResponse(
              "테스트 설명",
              null, // null ingredients는 NullPointerException 발생
              List.of("테스트"),
              1,
              5);

      // when & then
      org.junit.jupiter.api.Assertions.assertThrows(
          NullPointerException.class, () -> clientResponse.toRecipeDetail());
    }

    @Test
    @DisplayName("ClientRecipeDetailResponse.toRecipeDetail() - null이 아닌 필드들 정상 처리")
    void shouldHandlePartialNullValues() {
      // given
      ClientRecipeDetailResponse clientResponse =
          new ClientRecipeDetailResponse(
              null, // description은 null 허용
              List.of(), // 빈 리스트는 허용
              null, // tags는 null 허용
              null, // servings는 null 허용
              null // cookTime은 null 허용
              );

      // when
      RecipeDetail result = clientResponse.toRecipeDetail();

      // then
      assertThat(result).isNotNull();
      assertThat(result.description()).isNull();
      assertThat(result.ingredients()).isEmpty();
      assertThat(result.tags()).isNull();
      assertThat(result.servings()).isNull();
      assertThat(result.cookTime()).isNull();
    }
  }
}
