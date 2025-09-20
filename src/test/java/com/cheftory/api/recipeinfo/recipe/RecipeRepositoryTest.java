package com.cheftory.api.recipeinfo.recipe;

import static org.assertj.core.api.Assertions.assertThat;

import com.cheftory.api.DbContextTest;
import com.cheftory.api.recipeinfo.model.RecipeSort;
import com.cheftory.api.recipeinfo.recipe.entity.ProcessStep;
import com.cheftory.api.recipeinfo.recipe.entity.Recipe;
import com.cheftory.api.recipeinfo.recipe.entity.RecipeStatus;
import com.cheftory.api.recipeinfo.util.RecipePageRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DisplayName("RecipeRepositoryTest")
public class RecipeRepositoryTest extends DbContextTest {

  @Autowired private RecipeRepository recipeRepository;

  @Nested
  @DisplayName("레시피 저장")
  class SaveRecipe {

    @Nested
    @DisplayName("Given - 새로운 레시피가 주어졌을 때")
    class GivenNewRecipe {

      private Recipe recipe;

      @BeforeEach
      void setUp() {
        recipe = Recipe.create();
      }

      @Nested
      @DisplayName("When - 레시피를 저장하면")
      class WhenSaveRecipe {

        private Recipe savedRecipe;

        @BeforeEach
        void setUp() {
          savedRecipe = recipeRepository.save(recipe);
        }

        @DisplayName("Then - 레시피가 저장된다")
        @Test
        void thenRecipeIsSaved() {
          Optional<Recipe> foundRecipe = recipeRepository.findById(savedRecipe.getId());

          assertThat(foundRecipe).isPresent();
          assertThat(foundRecipe.get().getId()).isEqualTo(savedRecipe.getId());
          assertThat(foundRecipe.get().getProcessStep()).isEqualTo(ProcessStep.READY);
          assertThat(foundRecipe.get().getRecipeStatus()).isEqualTo(RecipeStatus.IN_PROGRESS);
          assertThat(foundRecipe.get().getViewCount()).isEqualTo(0);
          assertThat(foundRecipe.get().getCreatedAt()).isNotNull();
        }
      }
    }

    @Nested
    @DisplayName("Given - 성공 상태로 변경된 레시피가 주어졌을 때")
    class GivenSuccessRecipe {

      private Recipe recipe;

      @BeforeEach
      void setUp() {
        recipe = Recipe.create();
        recipe.success();
      }

      @Nested
      @DisplayName("When - 성공 상태 레시피를 저장하면")
      class WhenSaveSuccessRecipe {

        private Recipe savedRecipe;

        @BeforeEach
        void setUp() {
          savedRecipe = recipeRepository.save(recipe);
        }

        @DisplayName("Then - 성공 상태로 저장된다")
        @Test
        void thenSuccessRecipeIsSaved() {
          Optional<Recipe> foundRecipe = recipeRepository.findById(savedRecipe.getId());

          assertThat(foundRecipe).isPresent();
          assertThat(foundRecipe.get().getRecipeStatus()).isEqualTo(RecipeStatus.SUCCESS);
          assertThat(foundRecipe.get().isSuccess()).isTrue();
          assertThat(foundRecipe.get().isFailed()).isFalse();
        }
      }
    }

    @Nested
    @DisplayName("Given - 실패 상태로 변경된 레시피가 주어졌을 때")
    class GivenFailedRecipe {

      private Recipe recipe;

      @BeforeEach
      void setUp() {
        recipe = Recipe.create();
        recipe.failed();
      }

      @Nested
      @DisplayName("When - 실패 상태 레시피를 저장하면")
      class WhenSaveFailedRecipe {

        private Recipe savedRecipe;

        @BeforeEach
        void setUp() {
          savedRecipe = recipeRepository.save(recipe);
        }

        @DisplayName("Then - 실패 상태로 저장된다")
        @Test
        void thenFailedRecipeIsSaved() {
          Optional<Recipe> foundRecipe = recipeRepository.findById(savedRecipe.getId());

          assertThat(foundRecipe).isPresent();
          assertThat(foundRecipe.get().getRecipeStatus()).isEqualTo(RecipeStatus.FAILED);
          assertThat(foundRecipe.get().isSuccess()).isFalse();
          assertThat(foundRecipe.get().isFailed()).isTrue();
        }
      }
    }
  }

  @Nested
  @DisplayName("조회수 증가")
  class IncreaseViewCount {

    @Nested
    @DisplayName("Given - 저장된 레시피가 있을 때")
    class GivenSavedRecipe {

      private Recipe savedRecipe;

      @BeforeEach
      void setUp() {
        Recipe recipe = Recipe.create();
        savedRecipe = recipeRepository.save(recipe);
      }

      @Nested
      @DisplayName("When - 조회수를 증가시키면")
      class WhenIncreaseViewCount {

        @BeforeEach
        void setUp() {
          recipeRepository.increaseCount(savedRecipe.getId());
        }

        @DisplayName("Then - 조회수가 1 증가한다")
        @Test
        void thenViewCountIsIncreased() {
          Optional<Recipe> updatedRecipe = recipeRepository.findById(savedRecipe.getId());

          assertThat(updatedRecipe).isPresent();
          assertThat(updatedRecipe.get().getViewCount()).isEqualTo(1);
        }
      }

      @Nested
      @DisplayName("When - 조회수를 여러 번 증가시키면")
      class WhenIncreaseViewCountMultipleTimes {

        @BeforeEach
        void setUp() {
          recipeRepository.increaseCount(savedRecipe.getId());
          recipeRepository.increaseCount(savedRecipe.getId());
          recipeRepository.increaseCount(savedRecipe.getId());
        }

        @DisplayName("Then - 조회수가 3 증가한다")
        @Test
        void thenViewCountIsIncreasedByThree() {
          Optional<Recipe> updatedRecipe = recipeRepository.findById(savedRecipe.getId());

          assertThat(updatedRecipe).isPresent();
          assertThat(updatedRecipe.get().getViewCount()).isEqualTo(3);
        }
      }
    }

    @Nested
    @DisplayName("Given - 존재하지 않는 레시피 ID가 주어졌을 때")
    class GivenNonExistentRecipeId {

      private UUID nonExistentId;

      @BeforeEach
      void setUp() {
        nonExistentId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 존재하지 않는 ID로 조회수를 증가시키면")
      class WhenIncreaseViewCountWithNonExistentId {

        @DisplayName("Then - 아무 변화가 없다")
        @Test
        void thenNothingHappens() {
          // 예외가 발생하지 않고 정상적으로 처리되어야 함
          recipeRepository.increaseCount(nonExistentId);

          Optional<Recipe> recipe = recipeRepository.findById(nonExistentId);
          assertThat(recipe).isEmpty();
        }
      }
    }
  }

  @Nested
  @DisplayName("특정 상태가 아닌 레시피 조회")
  class FindRecipesByIdInAndRecipeStatusNot {

    private List<Recipe> savedRecipes;
    private List<UUID> recipeIds;

    @BeforeEach
    void setUp() {
      Recipe successRecipe1 = Recipe.create();
      successRecipe1.success();
      Recipe successRecipe2 = Recipe.create();
      successRecipe2.success();

      Recipe inProgressRecipe = Recipe.create();

      Recipe failedRecipe = Recipe.create();
      failedRecipe.failed();

      savedRecipes =
          recipeRepository.saveAll(
              List.of(successRecipe1, successRecipe2, inProgressRecipe, failedRecipe));

      recipeIds = savedRecipes.stream().map(Recipe::getId).toList();
    }

    @Nested
    @DisplayName("Given - 다양한 상태의 레시피들이 저장되어 있을 때")
    class GivenVariousStatusRecipes {

      @Nested
      @DisplayName("When - 실패 상태가 아닌 레시피들을 조회하면")
      class WhenFindRecipesNotFailed {

        private List<Recipe> notFailedRecipes;

        @BeforeEach
        void setUp() {
          notFailedRecipes =
              recipeRepository.findRecipesByIdInAndRecipeStatusNot(recipeIds, RecipeStatus.FAILED);
        }

        @DisplayName("Then - 실패 상태가 아닌 레시피들만 반환된다")
        @Test
        void thenReturnsRecipesNotFailed() {
          assertThat(notFailedRecipes).hasSize(3);

          notFailedRecipes.forEach(
              recipe -> {
                assertThat(recipe.getRecipeStatus()).isNotEqualTo(RecipeStatus.FAILED);
                assertThat(recipe.isFailed()).isFalse();
              });

          // 성공 상태와 진행 중 상태만 포함되어야 함
          long successCount =
              notFailedRecipes.stream()
                  .filter(recipe -> recipe.getRecipeStatus() == RecipeStatus.SUCCESS)
                  .count();
          long inProgressCount =
              notFailedRecipes.stream()
                  .filter(recipe -> recipe.getRecipeStatus() == RecipeStatus.IN_PROGRESS)
                  .count();

          assertThat(successCount).isEqualTo(2);
          assertThat(inProgressCount).isEqualTo(1);
        }
      }

      @Nested
      @DisplayName("When - 진행 중 상태가 아닌 레시피들을 조회하면")
      class WhenFindRecipesNotInProgress {

        private List<Recipe> notInProgressRecipes;

        @BeforeEach
        void setUp() {
          notInProgressRecipes =
              recipeRepository.findRecipesByIdInAndRecipeStatusNot(
                  recipeIds, RecipeStatus.IN_PROGRESS);
        }

        @DisplayName("Then - 진행 중 상태가 아닌 레시피들만 반환된다")
        @Test
        void thenReturnsRecipesNotInProgress() {
          assertThat(notInProgressRecipes).hasSize(3);

          notInProgressRecipes.forEach(
              recipe -> {
                assertThat(recipe.getRecipeStatus()).isNotEqualTo(RecipeStatus.IN_PROGRESS);
              });

          // 성공 상태와 실패 상태만 포함되어야 함
          long successCount =
              notInProgressRecipes.stream()
                  .filter(recipe -> recipe.getRecipeStatus() == RecipeStatus.SUCCESS)
                  .count();
          long failedCount =
              notInProgressRecipes.stream()
                  .filter(recipe -> recipe.getRecipeStatus() == RecipeStatus.FAILED)
                  .count();

          assertThat(successCount).isEqualTo(2);
          assertThat(failedCount).isEqualTo(1);
        }
      }
    }

    @Nested
    @DisplayName("Given - 빈 ID 목록이 주어졌을 때")
    class GivenEmptyIdList {

      @Nested
      @DisplayName("When - 빈 목록으로 조회하면")
      class WhenFindWithEmptyIdList {

        private List<Recipe> recipes;

        @BeforeEach
        void setUp() {
          recipes =
              recipeRepository.findRecipesByIdInAndRecipeStatusNot(List.of(), RecipeStatus.FAILED);
        }

        @DisplayName("Then - 빈 목록이 반환된다")
        @Test
        void thenReturnsEmptyList() {
          assertThat(recipes).isEmpty();
        }
      }
    }
  }

  @Nested
  @DisplayName("ID 목록으로 레시피 조회")
  class FindAllByIdIn {

    private List<Recipe> savedRecipes;
    private List<UUID> savedRecipeIds;

    @BeforeEach
    void setUp() {
      Recipe recipe1 = Recipe.create();
      Recipe recipe2 = Recipe.create();
      recipe2.success();
      Recipe recipe3 = Recipe.create();
      recipe3.failed();

      savedRecipes = recipeRepository.saveAll(List.of(recipe1, recipe2, recipe3));
      savedRecipeIds = savedRecipes.stream().map(Recipe::getId).toList();
    }

    @Nested
    @DisplayName("Given - 저장된 레시피 ID들이 주어졌을 때")
    class GivenSavedRecipeIds {

      @Nested
      @DisplayName("When - ID 목록으로 레시피들을 조회하면")
      class WhenFindAllByIdIn {

        private List<Recipe> foundRecipes;

        @BeforeEach
        void setUp() {
          foundRecipes = recipeRepository.findAllByIdIn(savedRecipeIds);
        }

        @DisplayName("Then - 해당 ID들의 모든 레시피가 반환된다")
        @Test
        void thenReturnsAllRecipesWithGivenIds() {
          assertThat(foundRecipes).hasSize(3);

          List<UUID> foundRecipeIds = foundRecipes.stream().map(Recipe::getId).toList();
          assertThat(foundRecipeIds).containsExactlyInAnyOrderElementsOf(savedRecipeIds);

          // 각 상태가 모두 포함되는지 확인
          boolean hasInProgress =
              foundRecipes.stream()
                  .anyMatch(recipe -> recipe.getRecipeStatus() == RecipeStatus.IN_PROGRESS);
          boolean hasSuccess =
              foundRecipes.stream()
                  .anyMatch(recipe -> recipe.getRecipeStatus() == RecipeStatus.SUCCESS);
          boolean hasFailed =
              foundRecipes.stream()
                  .anyMatch(recipe -> recipe.getRecipeStatus() == RecipeStatus.FAILED);

          assertThat(hasInProgress).isTrue();
          assertThat(hasSuccess).isTrue();
          assertThat(hasFailed).isTrue();
        }
      }
    }

    @Nested
    @DisplayName("Given - 일부 존재하고 일부 존재하지 않는 ID들이 주어졌을 때")
    class GivenPartiallyExistingIds {

      private List<UUID> mixedIds;

      @BeforeEach
      void setUp() {
        // 저장된 ID 2개 + 존재하지 않는 ID 2개
        mixedIds =
            List.of(
                savedRecipeIds.get(0), savedRecipeIds.get(1), UUID.randomUUID(), UUID.randomUUID());
      }

      @Nested
      @DisplayName("When - 혼합된 ID 목록으로 조회하면")
      class WhenFindAllByMixedIds {

        private List<Recipe> foundRecipes;

        @BeforeEach
        void setUp() {
          foundRecipes = recipeRepository.findAllByIdIn(mixedIds);
        }

        @DisplayName("Then - 존재하는 레시피들만 반환된다")
        @Test
        void thenReturnsOnlyExistingRecipes() {
          assertThat(foundRecipes).hasSize(2);

          List<UUID> foundRecipeIds = foundRecipes.stream().map(Recipe::getId).toList();
          assertThat(foundRecipeIds)
              .containsExactlyInAnyOrder(savedRecipeIds.get(0), savedRecipeIds.get(1));
        }
      }
    }

    @Nested
    @DisplayName("Given - 빈 ID 목록이 주어졌을 때")
    class GivenEmptyIdList {

      @Nested
      @DisplayName("When - 빈 목록으로 조회하면")
      class WhenFindAllByEmptyIdList {

        private List<Recipe> foundRecipes;

        @BeforeEach
        void setUp() {
          foundRecipes = recipeRepository.findAllByIdIn(List.of());
        }

        @DisplayName("Then - 빈 목록이 반환된다")
        @Test
        void thenReturnsEmptyList() {
          assertThat(foundRecipes).isEmpty();
        }
      }
    }
  }

  @Nested
  @DisplayName("상태별 레시피 페이징 조회")
  class FindByRecipeStatus {

    @BeforeEach
    void setUp() {
      Recipe successRecipe1 = Recipe.create();
      successRecipe1.success();
      Recipe successRecipe2 = Recipe.create();
      successRecipe2.success();
      Recipe successRecipe3 = Recipe.create();
      successRecipe3.success();
      Recipe successRecipe4 = Recipe.create();
      successRecipe4.success();
      Recipe successRecipe5 = Recipe.create();
      successRecipe5.success();

      Recipe inProgressRecipe1 = Recipe.create();
      Recipe inProgressRecipe2 = Recipe.create();

      Recipe failedRecipe1 = Recipe.create();
      failedRecipe1.failed();
      Recipe failedRecipe2 = Recipe.create();
      failedRecipe2.failed();

      recipeRepository.saveAll(
          List.of(
              successRecipe1,
              successRecipe2,
              successRecipe3,
              successRecipe4,
              successRecipe5,
              inProgressRecipe1,
              inProgressRecipe2,
              failedRecipe1,
              failedRecipe2));

      recipeRepository.increaseCount(successRecipe1.getId());
      recipeRepository.increaseCount(successRecipe1.getId());
      recipeRepository.increaseCount(successRecipe2.getId());
    }

    @Nested
    @DisplayName("Given - 다양한 상태의 레시피들이 저장되어 있을 때")
    class GivenVariousStatusRecipes {

      @Nested
      @DisplayName("When - 성공 상태 레시피들을 페이징 조회하면")
      class WhenFindSuccessRecipesWithPaging {

        @Test
        @DisplayName("Then - 성공 상태 레시피들만 반환된다")
        void thenReturnsOnlySuccessRecipes() {
          Pageable pageable = PageRequest.of(0, 10);

          Page<Recipe> result = recipeRepository.findByRecipeStatus(RecipeStatus.SUCCESS, pageable);

          assertThat(result.getContent()).isNotEmpty();
          assertThat(result.isFirst()).isTrue();

          result
              .getContent()
              .forEach(
                  recipe -> {
                    assertThat(recipe.getRecipeStatus()).isEqualTo(RecipeStatus.SUCCESS);
                    assertThat(recipe.isSuccess()).isTrue();
                    assertThat(recipe.isFailed()).isFalse();
                  });
        }

        @Test
        @DisplayName("Then - 페이지 크기만큼 제한되어 반환된다")
        void thenReturnsLimitedByPageSize() {
          Pageable pageable = PageRequest.of(0, 3);

          Page<Recipe> result = recipeRepository.findByRecipeStatus(RecipeStatus.SUCCESS, pageable);

          assertThat(result.getContent()).hasSize(3);
          assertThat(result.isFirst()).isTrue();
          assertThat(result.hasNext()).isTrue();
        }

        @Test
        @DisplayName("Then - 두 번째 페이지도 올바르게 반환된다")
        void thenReturnsSecondPageCorrectly() {
          Pageable pageable = PageRequest.of(1, 3);

          Page<Recipe> result = recipeRepository.findByRecipeStatus(RecipeStatus.SUCCESS, pageable);

          assertThat(result.isFirst()).isFalse();
          assertThat(result.hasPrevious()).isTrue();
        }
      }

      @Nested
      @DisplayName("When - 진행 중 상태 레시피들을 페이징 조회하면")
      class WhenFindInProgressRecipesWithPaging {

        @Test
        @DisplayName("Then - 진행 중 상태 레시피들만 반환된다")
        void thenReturnsOnlyInProgressRecipes() {
          Pageable pageable = PageRequest.of(0, 10);

          Page<Recipe> result =
              recipeRepository.findByRecipeStatus(RecipeStatus.IN_PROGRESS, pageable);

          assertThat(result.getContent()).isNotEmpty();

          result
              .getContent()
              .forEach(
                  recipe -> {
                    assertThat(recipe.getRecipeStatus()).isEqualTo(RecipeStatus.IN_PROGRESS);
                    assertThat(recipe.isSuccess()).isFalse();
                    assertThat(recipe.isFailed()).isFalse();
                  });
        }
      }

      @Nested
      @DisplayName("When - 실패 상태 레시피들을 페이징 조회하면")
      class WhenFindFailedRecipesWithPaging {

        @Test
        @DisplayName("Then - 실패 상태 레시피들만 반환된다")
        void thenReturnsOnlyFailedRecipes() {
          Pageable pageable = PageRequest.of(0, 10);

          Page<Recipe> result = recipeRepository.findByRecipeStatus(RecipeStatus.FAILED, pageable);

          assertThat(result.getContent()).isNotEmpty();

          result
              .getContent()
              .forEach(
                  recipe -> {
                    assertThat(recipe.getRecipeStatus()).isEqualTo(RecipeStatus.FAILED);
                    assertThat(recipe.isSuccess()).isFalse();
                    assertThat(recipe.isFailed()).isTrue();
                  });
        }
      }
    }

    @Nested
    @DisplayName("Given - 해당 상태의 레시피가 없을 때")
    class GivenNoRecipesWithStatus {

      @BeforeEach
      void setUp() {
        recipeRepository.deleteAll();
      }

      @Test
      @DisplayName("When - 해당 상태로 조회하면 Then - 빈 페이지가 반환된다")
      void whenFindByStatus_thenReturnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Recipe> result = recipeRepository.findByRecipeStatus(RecipeStatus.SUCCESS, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getTotalPages()).isEqualTo(0);
      }
    }

    @Nested
    @DisplayName("Given - 잘못된 페이지 요청이 주어졌을 때")
    class GivenInvalidPageRequest {

      @Test
      @DisplayName("When - 페이지 크기 0으로 조회하면 Then - IllegalArgumentException이 발생한다")
      void whenFindWithZeroPageSize_thenThrowsIllegalArgumentException() {
        org.junit.jupiter.api.Assertions.assertThrows(
            IllegalArgumentException.class, () -> PageRequest.of(0, 0));
      }

      @Test
      @DisplayName("When - 음수 페이지 크기로 조회하면 Then - IllegalArgumentException이 발생한다")
      void whenFindWithNegativePageSize_thenThrowsIllegalArgumentException() {
        org.junit.jupiter.api.Assertions.assertThrows(
            IllegalArgumentException.class, () -> PageRequest.of(0, -1));
      }

      @Test
      @DisplayName("When - 음수 페이지 번호로 조회하면 Then - IllegalArgumentException이 발생한다")
      void whenFindWithNegativePageNumber_thenThrowsIllegalArgumentException() {
        org.junit.jupiter.api.Assertions.assertThrows(
            IllegalArgumentException.class, () -> PageRequest.of(-1, 10));
      }

      @Test
      @DisplayName("When - 존재하지 않는 페이지를 조회하면 Then - 빈 페이지가 반환된다")
      void whenFindNonExistentPage_thenReturnsEmptyPage() {
        Pageable pageable = PageRequest.of(10, 10);

        Page<Recipe> result = recipeRepository.findByRecipeStatus(RecipeStatus.SUCCESS, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getNumber()).isEqualTo(10);
        assertThat(result.isFirst()).isFalse();
        assertThat(result.isLast()).isTrue();
      }
    }

    @Nested
    @DisplayName("Given - RecipePageRequest를 사용할 때")
    class GivenRecipePageRequest {

      @Test
      @DisplayName("When - RecipePageRequest.create로 조회수 내림차순 조회하면 Then - 조회수 순으로 정렬되어 반환된다")
      void whenFindWithRecipePageRequestCountDesc_thenReturnsOrderedByViewCount() {
        Pageable pageable = RecipePageRequest.create(0, RecipeSort.COUNT_DESC);

        Page<Recipe> result = recipeRepository.findByRecipeStatus(RecipeStatus.SUCCESS, pageable);

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getSize()).isEqualTo(10);

        List<Recipe> recipes = result.getContent();
        for (int i = 0; i < recipes.size() - 1; i++) {
          assertThat(recipes.get(i).getViewCount())
              .isGreaterThanOrEqualTo(recipes.get(i + 1).getViewCount());
        }

        assertThat(recipes.get(0).getViewCount()).isGreaterThanOrEqualTo(2);
      }

      @Test
      @DisplayName("When - RecipePageRequest.create로 1페이지 조회하면 Then - 두 번째 페이지가 반환된다")
      void whenFindSecondPageWithRecipePageRequest_thenReturnsSecondPage() {
        Pageable pageable = RecipePageRequest.create(1, RecipeSort.COUNT_DESC);

        Page<Recipe> result = recipeRepository.findByRecipeStatus(RecipeStatus.SUCCESS, pageable);

        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.isFirst()).isFalse();

        result
            .getContent()
            .forEach(
                recipe -> {
                  assertThat(recipe.getRecipeStatus()).isEqualTo(RecipeStatus.SUCCESS);
                });
      }

      @Test
      @DisplayName("When - RecipePageRequest.create로 다른 상태 조회하면 Then - 해당 상태만 반환된다")
      void whenFindInProgressWithRecipePageRequest_thenReturnsOnlyInProgress() {
        Pageable pageable = RecipePageRequest.create(0, RecipeSort.COUNT_DESC);

        Page<Recipe> result =
            recipeRepository.findByRecipeStatus(RecipeStatus.IN_PROGRESS, pageable);

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getSize()).isEqualTo(10);

        result
            .getContent()
            .forEach(
                recipe -> {
                  assertThat(recipe.getRecipeStatus()).isEqualTo(RecipeStatus.IN_PROGRESS);
                });

        List<Recipe> recipes = result.getContent();
        for (int i = 0; i < recipes.size() - 1; i++) {
          assertThat(recipes.get(i).getViewCount())
              .isGreaterThanOrEqualTo(recipes.get(i + 1).getViewCount());
        }
      }

      @Test
      @DisplayName("When - RecipePageRequest가 생성하는 Pageable 속성을 확인하면 Then - 올바른 설정이 적용된다")
      void whenCreateRecipePageRequest_thenCorrectPageableProperties() {
        Pageable pageable = RecipePageRequest.create(2, RecipeSort.COUNT_DESC);

        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getSort()).isEqualTo(RecipeSort.COUNT_DESC);
        assertThat(pageable.getOffset()).isEqualTo(20);
      }
    }
  }
}
