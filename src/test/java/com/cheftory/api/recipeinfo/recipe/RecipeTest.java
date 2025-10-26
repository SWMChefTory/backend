package com.cheftory.api.recipeinfo.recipe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.recipe.entity.ProcessStep;
import com.cheftory.api.recipeinfo.recipe.entity.Recipe;
import com.cheftory.api.recipeinfo.recipe.entity.RecipeStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeTest")
public class RecipeTest {

  private Clock clock;

  @BeforeEach
  void setUp() {
    clock = mock(Clock.class);
    doReturn(LocalDateTime.now()).when(clock).now();
  }

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
          recipe = Recipe.create(clock);
        }

        @DisplayName("Then - 레시피가 생성된다")
        @Test
        void thenRecipeIsCreated() {
          assertThat(recipe).isNotNull();
          assertThat(recipe.getId()).isNotNull();
          assertThat(recipe.getProcessStep()).isEqualTo(ProcessStep.READY);
          assertThat(recipe.getViewCount()).isEqualTo(0);
          assertThat(recipe.getCreatedAt()).isNotNull();
          assertThat(recipe.getUpdatedAt()).isNotNull();
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
        recipe = Recipe.create(clock);
      }

      @Nested
      @DisplayName("When - 레시피 상태를 성공으로 변경하면")
      class WhenChangeStatusToSuccess {

        @BeforeEach
        void setUp() {
          recipe.success(clock);
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
          recipe.failed(clock);
        }

        @DisplayName("Then - 레시피 상태가 실패로 변경된다")
        @Test
        void thenRecipeStatusIsFailed() {
          assertThat(recipe.isFailed()).isTrue();
          assertThat(recipe.isSuccess()).isFalse();
          assertThat(recipe.isBlocked()).isFalse();
          assertThat(recipe.getRecipeStatus()).isEqualTo(RecipeStatus.FAILED);
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 상태 확인")
  class CheckRecipeStatus {

    @Nested
    @DisplayName("Given - SUCCESS 상태의 레시피가 있을 때")
    class GivenSuccessRecipe {

      private Recipe recipe;

      @BeforeEach
      void setUp() {
        recipe = Recipe.create(clock);
        recipe.success(clock);
      }

      @Test
      @DisplayName("Then - isSuccess는 true, isFailed와 isBlocked는 false를 반환한다")
      void thenSuccessCheckMethodsWork() {
        assertThat(recipe.isSuccess()).isTrue();
        assertThat(recipe.isFailed()).isFalse();
        assertThat(recipe.isBlocked()).isFalse();
      }
    }

    @Nested
    @DisplayName("Given - FAILED 상태의 레시피가 있을 때")
    class GivenFailedRecipe {

      private Recipe recipe;

      @BeforeEach
      void setUp() {
        recipe = Recipe.create(clock);
        recipe.failed(clock);
      }

      @Test
      @DisplayName("Then - isFailed는 true, isSuccess와 isBlocked는 false를 반환한다")
      void thenFailedCheckMethodsWork() {
        assertThat(recipe.isFailed()).isTrue();
        assertThat(recipe.isSuccess()).isFalse();
        assertThat(recipe.isBlocked()).isFalse();
      }
    }

    @Nested
    @DisplayName("Given - IN_PROGRESS 상태의 레시피가 있을 때")
    class GivenInProgressRecipe {

      private Recipe recipe;

      @BeforeEach
      void setUp() {
        recipe = Recipe.create(clock);
      }

      @Test
      @DisplayName("Then - 모든 상태 체크 메서드가 false를 반환한다")
      void thenInProgressCheckMethodsWork() {
        assertThat(recipe.isSuccess()).isFalse();
        assertThat(recipe.isFailed()).isFalse();
        assertThat(recipe.isBlocked()).isFalse();
        assertThat(recipe.getRecipeStatus()).isEqualTo(RecipeStatus.IN_PROGRESS);
      }
    }
  }
}
