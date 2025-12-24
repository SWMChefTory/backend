package com.cheftory.api.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.category.RecipeCategoryRepository;
import com.cheftory.api.recipe.category.RecipeCategoryService;
import com.cheftory.api.recipe.category.entity.RecipeCategory;
import com.cheftory.api.recipe.category.entity.RecipeCategoryStatus;
import com.cheftory.api.recipe.category.exception.RecipeCategoryErrorCode;
import com.cheftory.api.recipe.category.exception.RecipeCategoryException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeCategoryService Tests")
public class RecipeInfoRecipeRecipeCategoryServiceTest {

  private RecipeCategoryRepository recipeCategoryRepository;
  private RecipeCategoryService recipeCategoryService;
  private Clock clock;

  @BeforeEach
  void setUp() {
    recipeCategoryRepository = mock(RecipeCategoryRepository.class);
    clock = mock(Clock.class);
    recipeCategoryService = new RecipeCategoryService(recipeCategoryRepository, clock);
  }

  @Nested
  @DisplayName("레시피 카테고리 생성")
  class CreateRecipeInfoRecipeCategory {

    @Nested
    @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
    class GivenValidParameters {

      private String categoryName;
      private UUID userId;
      private RecipeCategory recipeCategory;

      @BeforeEach
      void setUp() {
        categoryName = "한식";
        userId = UUID.randomUUID();
        recipeCategory = RecipeCategory.create(clock, categoryName, userId);
        doReturn(recipeCategory).when(recipeCategoryRepository).save(any(RecipeCategory.class));
      }

      @Nested
      @DisplayName("When - 레시피 카테고리를 생성한다면")
      class WhenCreatingRecipeInfoRecipeCategory {

        @Test
        @DisplayName("Then - 레시피 카테고리가 올바르게 생성되어야 한다")
        void thenShouldCreateRecipeCategoryCorrectly() {
          UUID categoryId = recipeCategoryService.create(categoryName, userId);

          assertThat(categoryId).isNotNull().isEqualTo(recipeCategory.getId());
          verify(recipeCategoryRepository)
              .save(
                  argThat(
                      category ->
                          category.getName().equals(categoryName)
                              && category.getUserId().equals(userId)
                              && category.getStatus() == RecipeCategoryStatus.ACTIVE));
        }
      }
    }

    @Nested
    @DisplayName("Given - 잘못된 파라미터가 주어졌을 때")
    class GivenInvalidParameters {

      private String categoryName;
      private UUID userId;

      @BeforeEach
      void setUp() {
        categoryName = "";
        userId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 레시피 카테고리를 생성한다면")
      class WhenCreatingRecipeInfoRecipeCategory {

        @Test
        @DisplayName("Then - 예외가 발생해야 한다")
        void thenShouldThrowException() {

          RecipeCategoryException exception =
              assertThrows(
                  RecipeCategoryException.class,
                  () -> {
                    recipeCategoryService.create(categoryName, userId);
                  });

          assertThat(exception.getErrorMessage())
              .isEqualTo(RecipeCategoryErrorCode.RECIPE_CATEGORY_NAME_EMPTY);
        }
      }
    }
  }

  @Nested
  @DisplayName("사용자 레시피 카테고리 조회")
  class FindUserRecipeInfoCategories {

    @Nested
    @DisplayName("Given - 유효한 사용자 ID가 주어졌을 때")
    class GivenValidUserId {

      private UUID userId;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        doReturn(
                List.of(
                    RecipeCategory.create(clock, "양식", userId),
                    RecipeCategory.create(clock, "한식", userId)))
            .when(recipeCategoryRepository)
            .findAllByUserIdAndStatus(userId, RecipeCategoryStatus.ACTIVE);
      }

      @Nested
      @DisplayName("When - 사용자의 레시피 카테고리를 조회한다면")
      class WhenFindingUserRecipeInfoCategories {

        @Test
        @DisplayName("Then - 해당 사용자의 레시피 카테고리 목록을 반환해야 한다")
        void thenShouldReturnUserRecipeCategories() {
          List<RecipeCategory> categories = recipeCategoryService.getUsers(userId);

          assertThat(categories)
              .hasSize(2)
              .allMatch(
                  category ->
                      category.getUserId().equals(userId)
                          && category.getStatus() == RecipeCategoryStatus.ACTIVE);
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 카테고리 삭제")
  class DeleteRecipeInfoRecipeCategory {

    @Nested
    @DisplayName("Given - 존재하는 카테고리 ID가 주어졌을 때")
    class GivenExistingRecipeCategoryId {

      private UUID recipeCategoryId;
      private RecipeCategory recipeCategory;
      private UUID userId;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        recipeCategoryId = UUID.randomUUID();
        recipeCategory = RecipeCategory.create(clock, "한식", userId);
        doReturn(Optional.of(recipeCategory))
            .when(recipeCategoryRepository)
            .findById(recipeCategoryId);
      }

      @Nested
      @DisplayName("When - 레시피 카테고리를 삭제한다면")
      class WhenDeletingRecipeInfoRecipeCategory {

        @Test
        @DisplayName("Then - 레시피 카테고리가 삭제되어야 한다")
        void thenShouldDeleteRecipeCategory() {
          recipeCategoryService.delete(recipeCategoryId);

          verify(recipeCategoryRepository).findById(recipeCategoryId);
          assertThat(recipeCategory.getStatus()).isEqualTo(RecipeCategoryStatus.DELETED);
        }
      }
    }

    @Nested
    @DisplayName("Given - 존재하지 않는 카테고리 ID가 주어졌을 때")
    class GivenNonExistentRecipeCategoryId {

      private UUID recipeCategoryId;

      @BeforeEach
      void setUp() {
        recipeCategoryId = UUID.randomUUID();
        doReturn(Optional.empty()).when(recipeCategoryRepository).findById(recipeCategoryId);
      }

      @Nested
      @DisplayName("When - 레시피 카테고리를 삭제한다면")
      class WhenDeletingRecipeInfoRecipeCategory {

        @Test
        @DisplayName("Then - 예외가 발생해야 한다")
        void thenShouldThrowException() {
          try {
            recipeCategoryService.delete(recipeCategoryId);
          } catch (RecipeCategoryException e) {
            assertThat(e.getErrorMessage())
                .isEqualTo(RecipeCategoryErrorCode.RECIPE_CATEGORY_NOT_FOUND);
          }
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 카테고리 존재 여부 확인")
  class ExistsRecipeInfoRecipeCategory {

    @Nested
    @DisplayName("Given - 존재하는 카테고리 ID가 주어졌을 때")
    class GivenExistingRecipeCategoryId {

      private UUID recipeCategoryId;

      @BeforeEach
      void setUp() {
        recipeCategoryId = UUID.randomUUID();
        doReturn(true).when(recipeCategoryRepository).existsById(recipeCategoryId);
      }

      @Nested
      @DisplayName("When - 레시피 카테고리 존재 여부를 확인한다면")
      class WhenCheckingExistsRecipeInfoRecipeCategory {

        @Test
        @DisplayName("Then - true를 반환해야 한다")
        void thenShouldReturnTrue() {
          boolean exists = recipeCategoryService.exists(recipeCategoryId);
          assertThat(exists).isTrue();
          verify(recipeCategoryRepository).existsById(recipeCategoryId);
        }
      }
    }

    @Nested
    @DisplayName("Given - 존재하지 않는 카테고리 ID가 주어졌을 때")
    class GivenNonExistentRecipeCategoryId {

      private UUID recipeCategoryId;

      @BeforeEach
      void setUp() {
        recipeCategoryId = UUID.randomUUID();
        doReturn(false).when(recipeCategoryRepository).existsById(recipeCategoryId);
      }

      @Nested
      @DisplayName("When - 레시피 카테고리 존재 여부를 확인한다면")
      class WhenCheckingExistsRecipeInfoRecipeCategory {

        @Test
        @DisplayName("Then - false를 반환해야 한다")
        void thenShouldReturnFalse() {
          boolean exists = recipeCategoryService.exists(recipeCategoryId);
          assertThat(exists).isFalse();
          verify(recipeCategoryRepository).existsById(recipeCategoryId);
        }
      }
    }
  }
}
