package com.cheftory.api.recipeinfo.recipe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.dto.RecipeSort;
import com.cheftory.api.recipeinfo.recipe.entity.ProcessStep;
import com.cheftory.api.recipeinfo.recipe.entity.Recipe;
import com.cheftory.api.recipeinfo.recipe.entity.RecipeStatus;
import com.cheftory.api.recipeinfo.tag.entity.RecipeTag;
import com.cheftory.api.recipeinfo.tag.RecipeTagRepository;
import com.cheftory.api.recipeinfo.util.RecipePageRequest;
import com.cheftory.api.recipeinfo.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipeinfo.youtubemeta.RecipeYoutubeMetaRepository;
import com.cheftory.api.recipeinfo.youtubemeta.entity.YoutubeMetaType;
import com.cheftory.api.recipeinfo.youtubemeta.entity.YoutubeVideoInfo;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DisplayName("RecipeRepositoryTest")
public class RecipeRepositoryTest extends DbContextTest {

  @Autowired private RecipeRepository recipeRepository;
  @Autowired private RecipeYoutubeMetaRepository youtubeMetaRepository;
  @Autowired private RecipeTagRepository recipeTagRepository;
  @MockitoBean private Clock clock;

  @Nested
  @DisplayName("레시피 저장")
  class SaveRecipe {

    @Nested
    @DisplayName("Given - 새로운 레시피가 주어졌을 때")
    class GivenNewRecipe {

      private Recipe recipe;

      @BeforeEach
      void setUp() {
        doReturn(LocalDateTime.now()).when(clock).now();
        recipe = Recipe.create(clock);
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
        doReturn(LocalDateTime.now()).when(clock).now();
        recipe = Recipe.create(clock);
        recipe.success(clock);
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
        doReturn(LocalDateTime.now()).when(clock).now();
        recipe = Recipe.create(clock);
        recipe.failed(clock);
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
        doReturn(LocalDateTime.now()).when(clock).now();
        Recipe recipe = Recipe.create(clock);
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
      doReturn(LocalDateTime.now()).when(clock).now();
      Recipe successRecipe1 = Recipe.create(clock);
      successRecipe1.success(clock);
      Recipe successRecipe2 = Recipe.create(clock);
      successRecipe2.success(clock);

      Recipe inProgressRecipe = Recipe.create(clock);

      Recipe failedRecipe = Recipe.create(clock);
      failedRecipe.failed(clock);

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
              recipeRepository.findRecipesByIdInAndRecipeStatusIn(
                  recipeIds, List.of(RecipeStatus.IN_PROGRESS, RecipeStatus.SUCCESS));
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
              recipeRepository.findRecipesByIdInAndRecipeStatusIn(
                  recipeIds, List.of(RecipeStatus.SUCCESS, RecipeStatus.FAILED));
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
              recipeRepository.findRecipesByIdInAndRecipeStatusIn(
                  List.of(), List.of(RecipeStatus.IN_PROGRESS, RecipeStatus.SUCCESS));
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
      doReturn(LocalDateTime.now()).when(clock).now();
      Recipe recipe1 = Recipe.create(clock);
      Recipe recipe2 = Recipe.create(clock);
      recipe2.success(clock);
      Recipe recipe3 = Recipe.create(clock);
      recipe3.failed(clock);

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
      doReturn(LocalDateTime.now()).when(clock).now();
      Recipe successRecipe1 = Recipe.create(clock);
      successRecipe1.success(clock);
      Recipe successRecipe2 = Recipe.create(clock);
      successRecipe2.success(clock);
      Recipe successRecipe3 = Recipe.create(clock);
      successRecipe3.success(clock);
      Recipe successRecipe4 = Recipe.create(clock);
      successRecipe4.success(clock);
      Recipe successRecipe5 = Recipe.create(clock);
      successRecipe5.success(clock);

      Recipe inProgressRecipe1 = Recipe.create(clock);
      Recipe inProgressRecipe2 = Recipe.create(clock);

      Recipe failedRecipe1 = Recipe.create(clock);
      failedRecipe1.failed(clock);
      Recipe failedRecipe2 = Recipe.create(clock);
      failedRecipe2.failed(clock);

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

  @Nested
  @DisplayName("레시피 존재 여부 확인")
  class ExistsById {

    @Nested
    @DisplayName("Given - 저장된 레시피가 있을 때")
    class GivenSavedRecipe {

      private Recipe savedRecipe;

      @BeforeEach
      void setUp() {
        doReturn(LocalDateTime.now()).when(clock).now();
        Recipe recipe = Recipe.create(clock);
        savedRecipe = recipeRepository.save(recipe);
      }

      @Nested
      @DisplayName("When - 저장된 레시피 ID로 존재 여부를 확인하면")
      class WhenCheckingExistenceWithSavedId {

        @Test
        @DisplayName("Then - true가 반환된다")
        void thenReturnTrue() {
          boolean exists = recipeRepository.existsById(savedRecipe.getId());

          assertThat(exists).isTrue();
        }
      }

      @Nested
      @DisplayName("When - 다른 상태의 레시피들이 존재할 때")
      class WhenDifferentStatusRecipesExist {

        private Recipe successRecipe;
        private Recipe failedRecipe;

        @BeforeEach
        void setUp() {
          doReturn(LocalDateTime.now()).when(clock).now();
          Recipe recipe1 = Recipe.create(clock);
          recipe1.success(clock);
          successRecipe = recipeRepository.save(recipe1);

          Recipe recipe2 = Recipe.create(clock);
          recipe2.failed(clock);
          failedRecipe = recipeRepository.save(recipe2);
        }

        @Test
        @DisplayName("Then - 모든 상태의 레시피가 존재하는 것으로 확인된다")
        void thenAllStatusRecipesExist() {
          assertThat(recipeRepository.existsById(savedRecipe.getId())).isTrue(); // IN_PROGRESS
          assertThat(recipeRepository.existsById(successRecipe.getId())).isTrue(); // SUCCESS
          assertThat(recipeRepository.existsById(failedRecipe.getId())).isTrue(); // FAILED
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
      @DisplayName("When - 존재하지 않는 ID로 존재 여부를 확인하면")
      class WhenCheckingExistenceWithNonExistentId {

        @Test
        @DisplayName("Then - false가 반환된다")
        void thenReturnFalse() {
          boolean exists = recipeRepository.existsById(nonExistentId);

          assertThat(exists).isFalse();
        }
      }
    }
  }

  @Nested
  @DisplayName("Repository 성능 및 엣지 케이스")
  class PerformanceAndEdgeCases {

    @Nested
    @DisplayName("Given - 대량의 레시피가 저장되어 있을 때")
    class GivenLargeNumberOfRecipes {

      @BeforeEach
      void setUp() {
        doReturn(LocalDateTime.now()).when(clock).now();
        List<Recipe> recipes = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
          Recipe recipe = Recipe.create(clock);
          if (i % 3 == 0) {
            recipe.success(clock);
          } else if (i % 5 == 0) {
            recipe.failed(clock);
          }
          recipes.add(recipe);
        }
        recipeRepository.saveAll(recipes);
      }

      @Nested
      @DisplayName("When - 대량 데이터에서 상태별 조회를 하면")
      class WhenQueryingLargeDataSet {

        @Test
        @DisplayName("Then - 성능 저하 없이 정상 조회된다")
        void thenPerformsWellWithLargeDataSet() {
          long startTime = System.currentTimeMillis();

          Pageable pageable = RecipePageRequest.create(0, RecipeSort.COUNT_DESC);
          Page<Recipe> successRecipes =
              recipeRepository.findByRecipeStatus(RecipeStatus.SUCCESS, pageable);
          Page<Recipe> inProgressRecipes =
              recipeRepository.findByRecipeStatus(RecipeStatus.IN_PROGRESS, pageable);
          Page<Recipe> failedRecipes =
              recipeRepository.findByRecipeStatus(RecipeStatus.FAILED, pageable);

          long endTime = System.currentTimeMillis();
          long executionTime = endTime - startTime;

          // 결과 검증
          assertThat(successRecipes.getContent()).isNotEmpty();
          assertThat(inProgressRecipes.getContent()).isNotEmpty();
          assertThat(failedRecipes.getContent()).isNotEmpty();

          long totalRecipes =
              successRecipes.getTotalElements()
                  + inProgressRecipes.getTotalElements()
                  + failedRecipes.getTotalElements();
          assertThat(totalRecipes).isGreaterThanOrEqualTo(100);
        }
      }
    }
  }

  @Nested
  @DisplayName("비디오 타입별 레시피 조회")
  class FindRecipes {

    @Nested
    @DisplayName("Given - NORMAL 타입 레시피들이 존재할 때")
    class GivenNormalRecipesExist {

      private UUID normalRecipeId1;
      private UUID normalRecipeId2;
      private UUID shortsRecipeId;
      private Pageable pageable;

      @BeforeEach
      void setUp() {
        pageable = RecipePageRequest.create(0, RecipeSort.COUNT_DESC);
        LocalDateTime now = LocalDateTime.now();
        doReturn(now).when(clock).now();

        // NORMAL 타입 레시피 생성
        Recipe normalRecipe1 = Recipe.create(clock);
        normalRecipe1.success(clock);
        normalRecipeId1 = recipeRepository.save(normalRecipe1).getId();
        createYoutubeMeta(normalRecipeId1, YoutubeMetaType.NORMAL);

        Recipe normalRecipe2 = Recipe.create(clock);
        normalRecipe2.success(clock);
        normalRecipeId2 = recipeRepository.save(normalRecipe2).getId();
        createYoutubeMeta(normalRecipeId2, YoutubeMetaType.NORMAL);

        // SHORTS 타입 레시피 생성
        Recipe shortsRecipe = Recipe.create(clock);
        shortsRecipe.success(clock);
        shortsRecipeId = recipeRepository.save(shortsRecipe).getId();
        createYoutubeMeta(shortsRecipeId, YoutubeMetaType.SHORTS);
      }

      @Nested
      @DisplayName("When - NORMAL 레시피를 조회하면")
      class WhenFindingNormalRecipes {

        @Test
        @DisplayName("Then - NORMAL 타입 레시피만 반환된다")
        void thenReturnsOnlyNormalRecipes() {
          Page<Recipe> result =
              recipeRepository.findRecipes(
                  RecipeStatus.SUCCESS, pageable, YoutubeMetaType.NORMAL.name());

          assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(2);
          assertThat(result.getContent())
              .extracting(Recipe::getId)
              .contains(normalRecipeId1, normalRecipeId2);
        }
      }
    }

    @Nested
    @DisplayName("Given - SHORTS 타입 레시피들이 존재할 때")
    class GivenShortsRecipesExist {

      private UUID shortsRecipeId1;
      private UUID shortsRecipeId2;
      private UUID normalRecipeId;
      private Pageable pageable;

      @BeforeEach
      void setUp() {
        pageable = RecipePageRequest.create(0, RecipeSort.COUNT_DESC);
        LocalDateTime now = LocalDateTime.now();
        doReturn(now).when(clock).now();

        Recipe shortsRecipe1 = Recipe.create(clock);
        shortsRecipe1.success(clock);
        shortsRecipeId1 = recipeRepository.save(shortsRecipe1).getId();
        createYoutubeMeta(shortsRecipeId1, YoutubeMetaType.SHORTS);

        Recipe shortsRecipe2 = Recipe.create(clock);
        shortsRecipe2.success(clock);
        shortsRecipeId2 = recipeRepository.save(shortsRecipe2).getId();
        createYoutubeMeta(shortsRecipeId2, YoutubeMetaType.SHORTS);

        Recipe normalRecipe = Recipe.create(clock);
        normalRecipe.success(clock);
        normalRecipeId = recipeRepository.save(normalRecipe).getId();
        createYoutubeMeta(normalRecipeId, YoutubeMetaType.NORMAL);
      }

      @Nested
      @DisplayName("When - SHORTS 레시피를 조회하면")
      class WhenFindingShortsRecipes {

        @Test
        @DisplayName("Then - SHORTS 타입 레시피만 반환된다")
        void thenReturnsOnlyShortsRecipes() {
          Page<Recipe> result =
              recipeRepository.findRecipes(
                  RecipeStatus.SUCCESS, pageable, YoutubeMetaType.SHORTS.name());

          assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(2);
          assertThat(result.getContent())
              .extracting(Recipe::getId)
              .contains(shortsRecipeId1, shortsRecipeId2);
        }
      }
    }
  }

  @Nested
  @DisplayName("태그별 레시피 조회")
  class FindCuisineRecipes {

    @Nested
    @DisplayName("Given - 한식 태그를 가진 레시피들이 존재할 때")
    class GivenKoreanRecipesExist {

      private UUID koreanRecipeId1;
      private UUID koreanRecipeId2;
      private UUID chineseRecipeId;
      private Pageable pageable;

      @BeforeEach
      void setUp() {
        pageable = RecipePageRequest.create(0, RecipeSort.COUNT_DESC);
        LocalDateTime now = LocalDateTime.now();
        doReturn(now).when(clock).now();

        // 한식 태그를 가진 레시피 생성
        Recipe koreanRecipe1 = Recipe.create(clock);
        koreanRecipe1.success(clock);
        koreanRecipeId1 = recipeRepository.save(koreanRecipe1).getId();
        createRecipeTag(koreanRecipeId1, "한식");

        Recipe koreanRecipe2 = Recipe.create(clock);
        koreanRecipe2.success(clock);
        koreanRecipeId2 = recipeRepository.save(koreanRecipe2).getId();
        createRecipeTag(koreanRecipeId2, "한식");

        // 중식 태그를 가진 레시피 생성
        Recipe chineseRecipe = Recipe.create(clock);
        chineseRecipe.success(clock);
        chineseRecipeId = recipeRepository.save(chineseRecipe).getId();
        createRecipeTag(chineseRecipeId, "중식");
      }

      @Nested
      @DisplayName("When - 한식 레시피를 조회하면")
      class WhenFindingKoreanRecipes {

        @Test
        @DisplayName("Then - 한식 태그를 가진 레시피만 반환된다")
        void thenReturnsOnlyKoreanRecipes() {
          Page<Recipe> result =
              recipeRepository.findCuisineRecipes("한식", RecipeStatus.SUCCESS, pageable);

          assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(2);
          assertThat(result.getContent())
              .extracting(Recipe::getId)
              .contains(koreanRecipeId1, koreanRecipeId2);
          assertThat(result.getContent()).extracting(Recipe::getId).doesNotContain(chineseRecipeId);
        }
      }
    }

    @Nested
    @DisplayName("Given - 중식 태그를 가진 레시피들이 존재할 때")
    class GivenChineseRecipesExist {

      private UUID chineseRecipeId1;
      private UUID chineseRecipeId2;
      private UUID japaneseRecipeId;
      private Pageable pageable;

      @BeforeEach
      void setUp() {
        pageable = RecipePageRequest.create(0, RecipeSort.COUNT_DESC);
        LocalDateTime now = LocalDateTime.now();
        doReturn(now).when(clock).now();

        Recipe chineseRecipe1 = Recipe.create(clock);
        chineseRecipe1.success(clock);
        chineseRecipeId1 = recipeRepository.save(chineseRecipe1).getId();
        createRecipeTag(chineseRecipeId1, "중식");

        Recipe chineseRecipe2 = Recipe.create(clock);
        chineseRecipe2.success(clock);
        chineseRecipeId2 = recipeRepository.save(chineseRecipe2).getId();
        createRecipeTag(chineseRecipeId2, "중식");

        Recipe japaneseRecipe = Recipe.create(clock);
        japaneseRecipe.success(clock);
        japaneseRecipeId = recipeRepository.save(japaneseRecipe).getId();
        createRecipeTag(japaneseRecipeId, "일식");
      }

      @Nested
      @DisplayName("When - 중식 레시피를 조회하면")
      class WhenFindingChineseRecipes {

        @Test
        @DisplayName("Then - 중식 태그를 가진 레시피만 반환된다")
        void thenReturnsOnlyChineseRecipes() {
          Page<Recipe> result =
              recipeRepository.findCuisineRecipes("중식", RecipeStatus.SUCCESS, pageable);

          assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(2);
          assertThat(result.getContent())
              .extracting(Recipe::getId)
              .contains(chineseRecipeId1, chineseRecipeId2);
          assertThat(result.getContent())
              .extracting(Recipe::getId)
              .doesNotContain(japaneseRecipeId);
        }
      }
    }

    @Nested
    @DisplayName("Given - 진행 중 상태의 태그 레시피가 존재할 때")
    class GivenInProgressTaggedRecipe {

      private UUID inProgressRecipeId;
      private UUID successRecipeId;
      private Pageable pageable;

      @BeforeEach
      void setUp() {
        pageable = RecipePageRequest.create(0, RecipeSort.COUNT_DESC);
        LocalDateTime now = LocalDateTime.now();
        doReturn(now).when(clock).now();

        Recipe inProgressRecipe = Recipe.create(clock);
        inProgressRecipeId = recipeRepository.save(inProgressRecipe).getId();
        createRecipeTag(inProgressRecipeId, "한식");

        Recipe successRecipe = Recipe.create(clock);
        successRecipe.success(clock);
        successRecipeId = recipeRepository.save(successRecipe).getId();
        createRecipeTag(successRecipeId, "한식");
      }

      @Nested
      @DisplayName("When - SUCCESS 상태의 한식 레시피를 조회하면")
      class WhenFindingSuccessKoreanRecipes {

        @Test
        @DisplayName("Then - SUCCESS 상태의 한식 태그 레시피만 반환된다")
        void thenReturnsOnlySuccessKoreanRecipes() {
          Page<Recipe> result =
              recipeRepository.findCuisineRecipes("한식", RecipeStatus.SUCCESS, pageable);

          assertThat(result.getContent()).extracting(Recipe::getId).contains(successRecipeId);
          assertThat(result.getContent())
              .extracting(Recipe::getId)
              .doesNotContain(inProgressRecipeId);
          result
              .getContent()
              .forEach(
                  recipe -> {
                    assertThat(recipe.getRecipeStatus()).isEqualTo(RecipeStatus.SUCCESS);
                  });
        }
      }
    }

    @Nested
    @DisplayName("Given - 여러 태그를 가진 레시피가 존재할 때")
    class GivenRecipeWithMultipleTags {

      private UUID multiTagRecipeId;
      private Pageable pageable;

      @BeforeEach
      void setUp() {
        pageable = RecipePageRequest.create(0, RecipeSort.COUNT_DESC);
        LocalDateTime now = LocalDateTime.now();
        doReturn(now).when(clock).now();

        Recipe multiTagRecipe = Recipe.create(clock);
        multiTagRecipe.success(clock);
        multiTagRecipeId = recipeRepository.save(multiTagRecipe).getId();
        createRecipeTag(multiTagRecipeId, "한식");
        createRecipeTag(multiTagRecipeId, "매운맛");
        createRecipeTag(multiTagRecipeId, "간단요리");
      }

      @Nested
      @DisplayName("When - 한식 태그로 조회하면")
      class WhenFindingByKoreanTag {

        @Test
        @DisplayName("Then - 한식 태그를 가진 레시피가 반환된다")
        void thenReturnsRecipeWithKoreanTag() {
          Page<Recipe> result =
              recipeRepository.findCuisineRecipes("한식", RecipeStatus.SUCCESS, pageable);

          assertThat(result.getContent()).extracting(Recipe::getId).contains(multiTagRecipeId);
        }
      }

      @Nested
      @DisplayName("When - 매운맛 태그로 조회하면")
      class WhenFindingBySpicyTag {

        @Test
        @DisplayName("Then - 매운맛 태그를 가진 레시피가 반환된다")
        void thenReturnsRecipeWithSpicyTag() {
          Page<Recipe> result =
              recipeRepository.findCuisineRecipes("매운맛", RecipeStatus.SUCCESS, pageable);

          assertThat(result.getContent()).extracting(Recipe::getId).contains(multiTagRecipeId);
        }
      }
    }

    @Nested
    @DisplayName("Given - 태그가 없는 레시피만 존재할 때")
    class GivenRecipeWithoutTags {

      private Pageable pageable;

      @BeforeEach
      void setUp() {
        pageable = RecipePageRequest.create(0, RecipeSort.COUNT_DESC);
        LocalDateTime now = LocalDateTime.now();
        doReturn(now).when(clock).now();

        Recipe recipeWithoutTag = Recipe.create(clock);
        recipeWithoutTag.success(clock);
        recipeRepository.save(recipeWithoutTag);
      }

      @Nested
      @DisplayName("When - 한식 태그로 조회하면")
      class WhenFindingByKoreanTag {

        @Test
        @DisplayName("Then - 빈 페이지가 반환된다")
        void thenReturnsEmptyPage() {
          Page<Recipe> result =
              recipeRepository.findCuisineRecipes("한식", RecipeStatus.SUCCESS, pageable);

          assertThat(result.getContent()).isEmpty();
          assertThat(result.getTotalElements()).isEqualTo(0);
        }
      }
    }
  }

  private void createYoutubeMeta(UUID recipeId, YoutubeMetaType type) {
    String videoId = "test_" + UUID.randomUUID().toString().substring(0, 8);
    YoutubeVideoInfo videoInfo = mock(YoutubeVideoInfo.class);
    doReturn(URI.create("https://www.youtube.com/watch?v=" + videoId))
        .when(videoInfo)
        .getVideoUri();
    doReturn(videoId).when(videoInfo).getVideoId();
    doReturn("Test Video").when(videoInfo).getTitle();
    doReturn(URI.create("https://img.youtube.com/vi/" + videoId + "/default.jpg"))
        .when(videoInfo)
        .getThumbnailUrl();
    doReturn(180).when(videoInfo).getVideoSeconds();
    doReturn(type).when(videoInfo).getVideoType();

    RecipeYoutubeMeta meta = RecipeYoutubeMeta.create(videoInfo, recipeId, clock);
    youtubeMetaRepository.save(meta);
  }

  private void createRecipeTag(UUID recipeId, String tag) {
    RecipeTag recipeTag = RecipeTag.create(tag, recipeId, clock);
    recipeTagRepository.save(recipeTag);
  }
}
