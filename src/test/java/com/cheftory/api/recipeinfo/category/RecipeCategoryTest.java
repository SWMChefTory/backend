package com.cheftory.api.recipeinfo.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.category.entity.RecipeCategory;
import com.cheftory.api.recipeinfo.category.entity.RecipeCategoryStatus;
import com.cheftory.api.recipeinfo.category.exception.RecipeCategoryErrorCode;
import com.cheftory.api.recipeinfo.category.exception.RecipeCategoryException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeCategory Tests")
public class RecipeCategoryTest {

  @Nested
  @DisplayName("레시피 카테고리 생성")
  class CreateRecipeCategory {

    @Nested
    @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
    class GivenValidParameters {

      private String categoryName;
      private UUID userId;
      private Clock clock;

      @BeforeEach
      void setUp() {
        categoryName = "한식";
        userId = UUID.randomUUID();
        clock = new Clock();
      }

      @Nested
      @DisplayName("When - 레시피 카테고리를 생성한다면")
      class WhenCreatingRecipeCategory {

        private RecipeCategory recipeCategory;

        @BeforeEach
        void beforeEach() {
          recipeCategory = RecipeCategory.create(clock, categoryName, userId);
        }

        @Test
        @DisplayName("Then - 올바른 속성으로 레시피 카테고리가 생성되어야 한다")
        void thenShouldCreateRecipeCategoryWithCorrectProperties() {
          assertThat(recipeCategory.getId()).isNotNull();
          assertThat(recipeCategory.getName()).isEqualTo("한식");
          assertThat(recipeCategory.getUserId()).isEqualTo(userId);
          assertThat(recipeCategory.getCreatedAt()).isBeforeOrEqualTo(clock.now());
          assertThat(recipeCategory.getStatus()).isEqualTo(RecipeCategoryStatus.ACTIVE);
        }
      }
    }

    @Nested
    @DisplayName("Given - 빈 카테고리 이름이 주어졌을 때")
    class GivenEmptyCategoryName {

      private String emptyCategoryName;
      private UUID userId;
      private Clock clock;

      @BeforeEach
      void setUp() {
        emptyCategoryName = "";
        userId = UUID.randomUUID();
        clock = new Clock();
      }

      @Nested
      @DisplayName("When - 레시피 카테고리를 생성한다면")
      class WhenCreatingRecipeCategory {

        @Test
        @DisplayName("Then - 예외가 발생해야 한다")
        void thenShouldThrowException() {
          assertThatThrownBy(() -> RecipeCategory.create(clock, emptyCategoryName, userId))
              .isInstanceOfSatisfying(
                  RecipeCategoryException.class,
                  ex ->
                      assertThat(ex.getErrorMessage())
                          .isEqualTo(RecipeCategoryErrorCode.RECIPE_CATEGORY_NAME_EMPTY));
        }
      }
    }

    @Nested
    @DisplayName("Given - null 카테고리 이름이 주어졌을 때")
    class GivenNullCategoryName {

      private String nullCategoryName;
      private UUID userId;
      private Clock clock;

      @BeforeEach
      void setUp() {
        nullCategoryName = null;
        userId = UUID.randomUUID();
        clock = new Clock();
      }

      @Nested
      @DisplayName("When - 레시피 카테고리를 생성한다면")
      class WhenCreatingRecipeCategory {

        @Test
        @DisplayName("Then - 예외가 발생해야 한다")
        void thenShouldThrowException() {
          assertThatThrownBy(() -> RecipeCategory.create(clock, nullCategoryName, userId))
              .isInstanceOfSatisfying(
                  RecipeCategoryException.class,
                  ex ->
                      assertThat(ex.getErrorMessage())
                          .isEqualTo(RecipeCategoryErrorCode.RECIPE_CATEGORY_NAME_EMPTY));
        }
      }
    }

    @Nested
    @DisplayName("Given - 공백만 있는 카테고리 이름이 주어졌을 때")
    class GivenBlankCategoryName {

      private String blankCategoryName;
      private UUID userId;
      private Clock clock;

      @BeforeEach
      void setUp() {
        blankCategoryName = "   ";
        userId = UUID.randomUUID();
        clock = new Clock();
      }

      @Nested
      @DisplayName("When - 레시피 카테고리를 생성한다면")
      class WhenCreatingRecipeCategory {

        @Test
        @DisplayName("Then - 예외가 발생해야 한다")
        void thenShouldThrowException() {
          assertThatThrownBy(() -> RecipeCategory.create(clock, blankCategoryName, userId))
              .isInstanceOfSatisfying(
                  RecipeCategoryException.class,
                  ex ->
                      assertThat(ex.getErrorMessage())
                          .isEqualTo(RecipeCategoryErrorCode.RECIPE_CATEGORY_NAME_EMPTY));
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 카테고리 삭제")
  class DeleteRecipeCategory {

    @Nested
    @DisplayName("Given - 활성 상태의 레시피 카테고리가 주어졌을 때")
    class GivenActiveRecipeCategory {

      private RecipeCategory recipeCategory;
      private String categoryName;
      private UUID userId;
      private Clock clock;

      @BeforeEach
      void setUp() {
        categoryName = "한식";
        userId = UUID.randomUUID();
        clock = new Clock();
        recipeCategory = RecipeCategory.create(clock, categoryName, userId);
      }

      @Nested
      @DisplayName("When - 레시피 카테고리를 삭제한다면")
      class WhenDeletingRecipeCategory {

        @BeforeEach
        void beforeEach() {
          recipeCategory.delete();
        }

        @Test
        @DisplayName("Then - 레시피 카테고리 상태가 삭제됨으로 변경되어야 한다")
        void thenShouldChangeStatusToDeleted() {
          assertThat(recipeCategory.getStatus()).isEqualTo(RecipeCategoryStatus.DELETED);
        }

        @Test
        @DisplayName("Then - 다른 속성들은 변경되지 않아야 한다")
        void thenShouldKeepOtherPropertiesUnchanged() {
          assertThat(recipeCategory.getName()).isEqualTo(categoryName);
          assertThat(recipeCategory.getUserId()).isEqualTo(userId);
          assertThat(recipeCategory.getId()).isNotNull();
          assertThat(recipeCategory.getCreatedAt()).isNotNull();
        }
      }
    }

    @Nested
    @DisplayName("Given - 이미 삭제된 레시피 카테고리가 주어졌을 때")
    class GivenAlreadyDeletedRecipeCategory {

      private RecipeCategory recipeCategory;
      private String categoryName;
      private UUID userId;
      private Clock clock;

      @BeforeEach
      void setUp() {
        categoryName = "한식";
        userId = UUID.randomUUID();
        clock = new Clock();
        recipeCategory = RecipeCategory.create(clock, categoryName, userId);
        recipeCategory.delete(); // 이미 삭제됨
      }

      @Nested
      @DisplayName("When - 레시피 카테고리를 다시 삭제한다면")
      class WhenDeletingAgain {

        @BeforeEach
        void beforeEach() {
          recipeCategory.delete();
        }

        @Test
        @DisplayName("Then - 상태는 여전히 삭제됨이어야 한다")
        void thenShouldRemainDeleted() {
          assertThat(recipeCategory.getStatus()).isEqualTo(RecipeCategoryStatus.DELETED);
        }
      }
    }
  }
}
