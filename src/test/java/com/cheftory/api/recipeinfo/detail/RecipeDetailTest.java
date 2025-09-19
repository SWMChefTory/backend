package com.cheftory.api.recipeinfo.detail;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeDetailTest")
public class RecipeDetailTest {

  @Nested
  @DisplayName("레시피 상세 정보 생성")
  class CreateRecipeDetail {

    @Nested
    @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
    class GivenValidParameters {

      private String description;
      private List<RecipeDetail.Ingredient> ingredients;
      private List<String> tags;
      private Integer servings;
      private Integer cookTime;

      @BeforeEach
      void setUp() {
        description = "맛있는 김치찌개 만들기";
        ingredients =
            List.of(
                RecipeDetail.Ingredient.of("김치", 200, "g"),
                RecipeDetail.Ingredient.of("돼지고기", 150, "g"),
                RecipeDetail.Ingredient.of("두부", 1, "모"),
                RecipeDetail.Ingredient.of("대파", 1, "대"));
        tags = List.of("한식", "찌개", "김치", "매운맛");
        servings = 2;
        cookTime = 30;
      }

      @Nested
      @DisplayName("When - 레시피 상세 정보를 생성하면")
      class WhenCreateRecipeDetail {

        private RecipeDetail recipeDetail;

        @BeforeEach
        void setUp() {
          recipeDetail = RecipeDetail.of(description, ingredients, tags, servings, cookTime);
        }

        @DisplayName("Then - 레시피 상세 정보가 생성된다")
        @Test
        void thenRecipeDetailIsCreated() {
          assertThat(recipeDetail).isNotNull();
          assertThat(recipeDetail.description()).isEqualTo("맛있는 김치찌개 만들기");
          assertThat(recipeDetail.ingredients()).hasSize(4);
          assertThat(recipeDetail.ingredients())
              .containsExactly(
                  RecipeDetail.Ingredient.of("김치", 200, "g"),
                  RecipeDetail.Ingredient.of("돼지고기", 150, "g"),
                  RecipeDetail.Ingredient.of("두부", 1, "모"),
                  RecipeDetail.Ingredient.of("대파", 1, "대"));
          assertThat(recipeDetail.tags()).hasSize(4);
          assertThat(recipeDetail.tags()).containsExactly("한식", "찌개", "김치", "매운맛");
          assertThat(recipeDetail.servings()).isEqualTo(2);
          assertThat(recipeDetail.cookTime()).isEqualTo(30);
        }

        @DisplayName("Then - 각 재료 정보가 올바르게 설정된다")
        @Test
        void thenIngredientInfoIsCorrect() {
          RecipeDetail.Ingredient firstIngredient = recipeDetail.ingredients().get(0);
          assertThat(firstIngredient.name()).isEqualTo("김치");
          assertThat(firstIngredient.amount()).isEqualTo(200);
          assertThat(firstIngredient.unit()).isEqualTo("g");

          RecipeDetail.Ingredient lastIngredient = recipeDetail.ingredients().get(3);
          assertThat(lastIngredient.name()).isEqualTo("대파");
          assertThat(lastIngredient.amount()).isEqualTo(1);
          assertThat(lastIngredient.unit()).isEqualTo("대");
        }
      }
    }

    @Nested
    @DisplayName("Given - 간단한 레시피 파라미터가 주어졌을 때")
    class GivenSimpleRecipeParameters {

      private String description;
      private List<RecipeDetail.Ingredient> ingredients;
      private List<String> tags;
      private Integer servings;
      private Integer cookTime;

      @BeforeEach
      void setUp() {
        description = "간단한 계란찜";
        ingredients = List.of(RecipeDetail.Ingredient.of("계란", 3, "개"));
        tags = List.of("간식");
        servings = 1;
        cookTime = 5;
      }

      @Nested
      @DisplayName("When - 간단한 레시피를 생성하면")
      class WhenCreateSimpleRecipe {

        private RecipeDetail recipeDetail;

        @BeforeEach
        void setUp() {
          recipeDetail = RecipeDetail.of(description, ingredients, tags, servings, cookTime);
        }

        @DisplayName("Then - 간단한 레시피가 생성된다")
        @Test
        void thenSimpleRecipeIsCreated() {
          assertThat(recipeDetail).isNotNull();
          assertThat(recipeDetail.description()).isEqualTo("간단한 계란찜");
          assertThat(recipeDetail.ingredients()).hasSize(1);
          assertThat(recipeDetail.ingredients().get(0).name()).isEqualTo("계란");
          assertThat(recipeDetail.tags()).hasSize(1);
          assertThat(recipeDetail.tags()).containsExactly("간식");
          assertThat(recipeDetail.servings()).isEqualTo(1);
          assertThat(recipeDetail.cookTime()).isEqualTo(5);
        }
      }
    }

    @Nested
    @DisplayName("Given - 빈 목록들이 주어졌을 때")
    class GivenEmptyLists {

      private String description;
      private List<RecipeDetail.Ingredient> ingredients;
      private List<String> tags;
      private Integer servings;
      private Integer cookTime;

      @BeforeEach
      void setUp() {
        description = "재료 없는 레시피";
        ingredients = List.of();
        tags = List.of();
        servings = 0;
        cookTime = 0;
      }

      @Nested
      @DisplayName("When - 빈 목록으로 레시피를 생성하면")
      class WhenCreateRecipeWithEmptyLists {

        private RecipeDetail recipeDetail;

        @BeforeEach
        void setUp() {
          recipeDetail = RecipeDetail.of(description, ingredients, tags, servings, cookTime);
        }

        @DisplayName("Then - 빈 목록을 가진 레시피가 생성된다")
        @Test
        void thenRecipeWithEmptyListsIsCreated() {
          assertThat(recipeDetail).isNotNull();
          assertThat(recipeDetail.description()).isEqualTo("재료 없는 레시피");
          assertThat(recipeDetail.ingredients()).isEmpty();
          assertThat(recipeDetail.tags()).isEmpty();
          assertThat(recipeDetail.servings()).isEqualTo(0);
          assertThat(recipeDetail.cookTime()).isEqualTo(0);
        }
      }
    }
  }

  @Nested
  @DisplayName("재료 생성")
  class CreateIngredient {

    @Nested
    @DisplayName("Given - 유효한 재료 파라미터가 주어졌을 때")
    class GivenValidIngredientParameters {

      private String name;
      private Integer amount;
      private String unit;

      @BeforeEach
      void setUp() {
        name = "양파";
        amount = 1;
        unit = "개";
      }

      @Nested
      @DisplayName("When - 재료를 생성하면")
      class WhenCreateIngredient {

        private RecipeDetail.Ingredient ingredient;

        @BeforeEach
        void setUp() {
          ingredient = RecipeDetail.Ingredient.of(name, amount, unit);
        }

        @DisplayName("Then - 재료가 생성된다")
        @Test
        void thenIngredientIsCreated() {
          assertThat(ingredient).isNotNull();
          assertThat(ingredient.name()).isEqualTo("양파");
          assertThat(ingredient.amount()).isEqualTo(1);
          assertThat(ingredient.unit()).isEqualTo("개");
        }
      }
    }

    @Nested
    @DisplayName("Given - 다양한 단위의 재료 파라미터가 주어졌을 때")
    class GivenVariousUnitIngredients {

      @DisplayName("When - 그램 단위 재료를 생성하면 Then - 올바른 재료가 생성된다")
      @Test
      void whenCreateGramIngredient_thenCorrectIngredientIsCreated() {
        RecipeDetail.Ingredient ingredient = RecipeDetail.Ingredient.of("소금", 5, "g");

        assertThat(ingredient.name()).isEqualTo("소금");
        assertThat(ingredient.amount()).isEqualTo(5);
        assertThat(ingredient.unit()).isEqualTo("g");
      }

      @DisplayName("When - 큰술 단위 재료를 생성하면 Then - 올바른 재료가 생성된다")
      @Test
      void whenCreateTablespoonIngredient_thenCorrectIngredientIsCreated() {
        RecipeDetail.Ingredient ingredient = RecipeDetail.Ingredient.of("올리브오일", 2, "큰술");

        assertThat(ingredient.name()).isEqualTo("올리브오일");
        assertThat(ingredient.amount()).isEqualTo(2);
        assertThat(ingredient.unit()).isEqualTo("큰술");
      }

      @DisplayName("When - 컵 단위 재료를 생성하면 Then - 올바른 재료가 생성된다")
      @Test
      void whenCreateCupIngredient_thenCorrectIngredientIsCreated() {
        RecipeDetail.Ingredient ingredient = RecipeDetail.Ingredient.of("물", 1, "컵");

        assertThat(ingredient.name()).isEqualTo("물");
        assertThat(ingredient.amount()).isEqualTo(1);
        assertThat(ingredient.unit()).isEqualTo("컵");
      }
    }

    @Nested
    @DisplayName("Given - 특수한 값의 재료 파라미터가 주어졌을 때")
    class GivenSpecialValueIngredients {

      @DisplayName("When - 0 수량 재료를 생성하면 Then - 올바른 재료가 생성된다")
      @Test
      void whenCreateZeroAmountIngredient_thenCorrectIngredientIsCreated() {
        RecipeDetail.Ingredient ingredient = RecipeDetail.Ingredient.of("장식용 파슬리", 0, "조금");

        assertThat(ingredient.name()).isEqualTo("장식용 파슬리");
        assertThat(ingredient.amount()).isEqualTo(0);
        assertThat(ingredient.unit()).isEqualTo("조금");
      }

      @DisplayName("When - 큰 수량 재료를 생성하면 Then - 올바른 재료가 생성된다")
      @Test
      void whenCreateLargeAmountIngredient_thenCorrectIngredientIsCreated() {
        RecipeDetail.Ingredient ingredient = RecipeDetail.Ingredient.of("쌀", 1000, "g");

        assertThat(ingredient.name()).isEqualTo("쌀");
        assertThat(ingredient.amount()).isEqualTo(1000);
        assertThat(ingredient.unit()).isEqualTo("g");
      }
    }
  }
}
