package com.cheftory.api.recipeinfo.recipe;

import static org.assertj.core.api.Assertions.assertThat;

import com.cheftory.api.recipeinfo.recipe.entity.ProcessStep;
import com.cheftory.api.recipeinfo.recipe.entity.Recipe;
import com.cheftory.api.recipeinfo.recipe.entity.RecipeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeTest")
public class RecipeTest {

  @Nested
  @DisplayName("레시피 생성")
  class CreateRecipe {

    @Nested
    @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
    class GivenValidParameters {

      @Nested
      @DisplayName("When - 레시피를 생성하면")
      class WhenCreateRecipe {

        private Recipe recipe;

        @BeforeEach
        void setUp() {
          recipe = Recipe.create();
        }

        @DisplayName("Then - 레시피가 생성된다")
        @Test
        void thenRecipeIsCreated() {
          assertThat(recipe).isNotNull();
          assertThat(recipe.getId()).isNotNull();
          assertThat(recipe.getProcessStep()).isEqualTo(ProcessStep.READY);
          assertThat(recipe.getViewCount()).isEqualTo(0);
          assertThat(recipe.getCreatedAt()).isNotNull();
          assertThat(recipe.getUpdatedAt()).isNull();
          assertThat(recipe.getRecipeStatus()).isEqualTo(RecipeStatus.IN_PROGRESS);
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 상태 변경")
  class ChangeRecipeStatus {

    @Nested
    @DisplayName("Given - 레시피가 생성되어 있을 때")
    class GivenRecipeCreated {

      private Recipe recipe;

      @BeforeEach
      void setUp() {
        recipe = Recipe.create();
      }

      @Nested
      @DisplayName("When - 레시피 상태를 성공으로 변경하면")
      class WhenChangeStatusToSuccess {

        @BeforeEach
        void setUp() {
          recipe.success();
        }

        @DisplayName("Then - 레시피 상태가 성공으로 변경된다")
        @Test
        void thenRecipeStatusIsSuccess() {
          assertThat(recipe.isSuccess()).isTrue();
          assertThat(recipe.isFailed()).isFalse();
          assertThat(recipe.getRecipeStatus()).isEqualTo(RecipeStatus.SUCCESS);
        }
      }

      @Nested
      @DisplayName("When - 레시피 상태를 실패로 변경하면")
      class WhenChangeStatusToFailed {

        @BeforeEach
        void setUp() {
          recipe.failed();
        }

        @DisplayName("Then - 레시피 상태가 실패로 변경된다")
        @Test
        void thenRecipeStatusIsFailed() {
          assertThat(recipe.isFailed()).isTrue();
          assertThat(recipe.isSuccess()).isFalse();
          assertThat(recipe.getRecipeStatus()).isEqualTo(RecipeStatus.FAILED);
        }
      }
    }
  }
}
