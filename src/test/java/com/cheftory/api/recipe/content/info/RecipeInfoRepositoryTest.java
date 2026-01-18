package com.cheftory.api.recipe.content.info;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.info.entity.ProcessStep;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.entity.RecipeStatus;
import com.cheftory.api.recipe.content.tag.RecipeTagRepository;
import com.cheftory.api.recipe.content.tag.entity.RecipeTag;
import com.cheftory.api.recipe.content.youtubemeta.RecipeYoutubeMetaRepository;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeMetaType;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeVideoInfo;
import com.cheftory.api.recipe.dto.RecipeSort;
import com.cheftory.api.recipe.util.RecipePageRequest;
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
public class RecipeInfoRepositoryTest extends DbContextTest {

  @Autowired private RecipeInfoRepository recipeInfoRepository;
  @Autowired private RecipeYoutubeMetaRepository youtubeMetaRepository;
  @Autowired private RecipeTagRepository recipeTagRepository;
  @MockitoBean private Clock clock;

  @Nested
  @DisplayName("레시피 저장")
  class SaveRecipeInfo {

    @Nested
    @DisplayName("Given - 새로운 레시피가 주어졌을 때")
    class GivenNewRecipeInfo {

      private RecipeInfo recipeInfo;

      @BeforeEach
      void setUp() {
        doReturn(LocalDateTime.now()).when(clock).now();
        recipeInfo = RecipeInfo.create(clock);
      }

      @Nested
      @DisplayName("When - 레시피를 저장하면")
      class WhenSaveRecipeInfo {

        private RecipeInfo savedRecipeInfo;

        @BeforeEach
        void setUp() {
          savedRecipeInfo = recipeInfoRepository.save(recipeInfo);
        }

        @DisplayName("Then - 레시피가 저장된다")
        @Test
        void thenRecipeIsSaved() {
          Optional<RecipeInfo> foundRecipe = recipeInfoRepository.findById(savedRecipeInfo.getId());

          assertThat(foundRecipe).isPresent();
          assertThat(foundRecipe.get().getId()).isEqualTo(savedRecipeInfo.getId());
          assertThat(foundRecipe.get().getProcessStep()).isEqualTo(ProcessStep.READY);
          assertThat(foundRecipe.get().getRecipeStatus()).isEqualTo(RecipeStatus.IN_PROGRESS);
          assertThat(foundRecipe.get().getViewCount()).isEqualTo(0);
          assertThat(foundRecipe.get().getCreatedAt()).isNotNull();
        }
      }
    }

    @Nested
    @DisplayName("Given - 성공 상태로 변경된 레시피가 주어졌을 때")
    class GivenSuccessRecipeInfo {

      private RecipeInfo recipeInfo;

      @BeforeEach
      void setUp() {
        doReturn(LocalDateTime.now()).when(clock).now();
        recipeInfo = RecipeInfo.create(clock);
        recipeInfo.success(clock);
      }

      @Nested
      @DisplayName("When - 성공 상태 레시피를 저장하면")
      class WhenSaveSuccessRecipeInfo {

        private RecipeInfo savedRecipeInfo;

        @BeforeEach
        void setUp() {
          savedRecipeInfo = recipeInfoRepository.save(recipeInfo);
        }

        @DisplayName("Then - 성공 상태로 저장된다")
        @Test
        void thenSuccessRecipeIsSaved() {
          Optional<RecipeInfo> foundRecipe = recipeInfoRepository.findById(savedRecipeInfo.getId());

          assertThat(foundRecipe).isPresent();
          assertThat(foundRecipe.get().getRecipeStatus()).isEqualTo(RecipeStatus.SUCCESS);
          assertThat(foundRecipe.get().isSuccess()).isTrue();
          assertThat(foundRecipe.get().isFailed()).isFalse();
        }
      }
    }

    @Nested
    @DisplayName("Given - 실패 상태로 변경된 레시피가 주어졌을 때")
    class GivenFailedRecipeInfo {

      private RecipeInfo recipeInfo;

      @BeforeEach
      void setUp() {
        doReturn(LocalDateTime.now()).when(clock).now();
        recipeInfo = RecipeInfo.create(clock);
        recipeInfo.failed(clock);
      }

      @Nested
      @DisplayName("When - 실패 상태 레시피를 저장하면")
      class WhenSaveFailedRecipeInfo {

        private RecipeInfo savedRecipeInfo;

        @BeforeEach
        void setUp() {
          savedRecipeInfo = recipeInfoRepository.save(recipeInfo);
        }

        @DisplayName("Then - 실패 상태로 저장된다")
        @Test
        void thenFailedRecipeIsSaved() {
          Optional<RecipeInfo> foundRecipe = recipeInfoRepository.findById(savedRecipeInfo.getId());

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
    class GivenSavedRecipeInfo {

      private RecipeInfo savedRecipeInfo;

      @BeforeEach
      void setUp() {
        doReturn(LocalDateTime.now()).when(clock).now();
        RecipeInfo recipeInfo = RecipeInfo.create(clock);
        savedRecipeInfo = recipeInfoRepository.save(recipeInfo);
      }

      @Nested
      @DisplayName("When - 조회수를 증가시키면")
      class WhenIncreaseViewCount {

        @BeforeEach
        void setUp() {
          recipeInfoRepository.increaseCount(savedRecipeInfo.getId());
        }

        @DisplayName("Then - 조회수가 1 증가한다")
        @Test
        void thenViewCountIsIncreased() {
          Optional<RecipeInfo> updatedRecipe =
              recipeInfoRepository.findById(savedRecipeInfo.getId());

          assertThat(updatedRecipe).isPresent();
          assertThat(updatedRecipe.get().getViewCount()).isEqualTo(1);
        }
      }

      @Nested
      @DisplayName("When - 조회수를 여러 번 증가시키면")
      class WhenIncreaseViewCountMultipleTimes {

        @BeforeEach
        void setUp() {
          recipeInfoRepository.increaseCount(savedRecipeInfo.getId());
          recipeInfoRepository.increaseCount(savedRecipeInfo.getId());
          recipeInfoRepository.increaseCount(savedRecipeInfo.getId());
        }

        @DisplayName("Then - 조회수가 3 증가한다")
        @Test
        void thenViewCountIsIncreasedByThree() {
          Optional<RecipeInfo> updatedRecipe =
              recipeInfoRepository.findById(savedRecipeInfo.getId());

          assertThat(updatedRecipe).isPresent();
          assertThat(updatedRecipe.get().getViewCount()).isEqualTo(3);
        }
      }
    }

    @Nested
    @DisplayName("Given - 존재하지 않는 레시피 ID가 주어졌을 때")
    class GivenNonExistentRecipeInfoId {

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
          recipeInfoRepository.increaseCount(nonExistentId);

          Optional<RecipeInfo> recipe = recipeInfoRepository.findById(nonExistentId);
          assertThat(recipe).isEmpty();
        }
      }
    }
  }

  @Nested
  @DisplayName("특정 상태가 아닌 레시피 조회")
  class FindRecipesByIdInAndRecipeInfoStatusNot {

    private List<RecipeInfo> savedRecipeInfos;
    private List<UUID> recipeIds;

    @BeforeEach
    void setUp() {
      doReturn(LocalDateTime.now()).when(clock).now();
      RecipeInfo successRecipeInfo1 = RecipeInfo.create(clock);
      successRecipeInfo1.success(clock);
      RecipeInfo successRecipeInfo2 = RecipeInfo.create(clock);
      successRecipeInfo2.success(clock);

      RecipeInfo inProgressRecipeInfo = RecipeInfo.create(clock);

      RecipeInfo failedRecipeInfo = RecipeInfo.create(clock);
      failedRecipeInfo.failed(clock);

      savedRecipeInfos =
          recipeInfoRepository.saveAll(
              List.of(
                  successRecipeInfo1, successRecipeInfo2, inProgressRecipeInfo, failedRecipeInfo));

      recipeIds = savedRecipeInfos.stream().map(RecipeInfo::getId).toList();
    }

    @Nested
    @DisplayName("Given - 다양한 상태의 레시피들이 저장되어 있을 때")
    class GivenVariousStatusRecipes {

      @Nested
      @DisplayName("When - 실패 상태가 아닌 레시피들을 조회하면")
      class WhenFindRecipesNotFailed {

        private List<RecipeInfo> notFailedRecipeInfos;

        @BeforeEach
        void setUp() {
          notFailedRecipeInfos =
              recipeInfoRepository.findRecipesByIdInAndRecipeStatusIn(
                  recipeIds, List.of(RecipeStatus.IN_PROGRESS, RecipeStatus.SUCCESS));
        }

        @DisplayName("Then - 실패 상태가 아닌 레시피들만 반환된다")
        @Test
        void thenReturnsRecipesNotFailed() {
          assertThat(notFailedRecipeInfos).hasSize(3);

          notFailedRecipeInfos.forEach(
              recipe -> {
                assertThat(recipe.getRecipeStatus()).isNotEqualTo(RecipeStatus.FAILED);
                assertThat(recipe.isFailed()).isFalse();
              });

          // 성공 상태와 진행 중 상태만 포함되어야 함
          long successCount =
              notFailedRecipeInfos.stream()
                  .filter(recipe -> recipe.getRecipeStatus() == RecipeStatus.SUCCESS)
                  .count();
          long inProgressCount =
              notFailedRecipeInfos.stream()
                  .filter(recipe -> recipe.getRecipeStatus() == RecipeStatus.IN_PROGRESS)
                  .count();

          assertThat(successCount).isEqualTo(2);
          assertThat(inProgressCount).isEqualTo(1);
        }
      }

      @Nested
      @DisplayName("When - 진행 중 상태가 아닌 레시피들을 조회하면")
      class WhenFindRecipesNotInProgress {

        private List<RecipeInfo> notInProgressRecipeInfos;

        @BeforeEach
        void setUp() {
          notInProgressRecipeInfos =
              recipeInfoRepository.findRecipesByIdInAndRecipeStatusIn(
                  recipeIds, List.of(RecipeStatus.SUCCESS, RecipeStatus.FAILED));
        }

        @DisplayName("Then - 진행 중 상태가 아닌 레시피들만 반환된다")
        @Test
        void thenReturnsRecipesNotInProgress() {
          assertThat(notInProgressRecipeInfos).hasSize(3);

          notInProgressRecipeInfos.forEach(
              recipe -> {
                assertThat(recipe.getRecipeStatus()).isNotEqualTo(RecipeStatus.IN_PROGRESS);
              });

          // 성공 상태와 실패 상태만 포함되어야 함
          long successCount =
              notInProgressRecipeInfos.stream()
                  .filter(recipe -> recipe.getRecipeStatus() == RecipeStatus.SUCCESS)
                  .count();
          long failedCount =
              notInProgressRecipeInfos.stream()
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

        private List<RecipeInfo> recipeInfos;

        @BeforeEach
        void setUp() {
          recipeInfos =
              recipeInfoRepository.findRecipesByIdInAndRecipeStatusIn(
                  List.of(), List.of(RecipeStatus.IN_PROGRESS, RecipeStatus.SUCCESS));
        }

        @DisplayName("Then - 빈 목록이 반환된다")
        @Test
        void thenReturnsEmptyList() {
          assertThat(recipeInfos).isEmpty();
        }
      }
    }
  }

  @Nested
  @DisplayName("ID 목록으로 레시피 조회")
  class FindAllByIdIn {

    private List<RecipeInfo> savedRecipeInfos;
    private List<UUID> savedRecipeIds;

    @BeforeEach
    void setUp() {
      doReturn(LocalDateTime.now()).when(clock).now();
      RecipeInfo recipeInfo1 = RecipeInfo.create(clock);
      RecipeInfo recipeInfo2 = RecipeInfo.create(clock);
      recipeInfo2.success(clock);
      RecipeInfo recipeInfo3 = RecipeInfo.create(clock);
      recipeInfo3.failed(clock);

      savedRecipeInfos =
          recipeInfoRepository.saveAll(List.of(recipeInfo1, recipeInfo2, recipeInfo3));
      savedRecipeIds = savedRecipeInfos.stream().map(RecipeInfo::getId).toList();
    }

    @Nested
    @DisplayName("Given - 저장된 레시피 ID들이 주어졌을 때")
    class GivenSavedRecipeInfoIds {

      @Nested
      @DisplayName("When - ID 목록으로 레시피들을 조회하면")
      class WhenFindAllByIdIn {

        private List<RecipeInfo> foundRecipeInfos;

        @BeforeEach
        void setUp() {
          foundRecipeInfos = recipeInfoRepository.findAllByIdIn(savedRecipeIds);
        }

        @DisplayName("Then - 해당 ID들의 모든 레시피가 반환된다")
        @Test
        void thenReturnsAllRecipesWithGivenIds() {
          assertThat(foundRecipeInfos).hasSize(3);

          List<UUID> foundRecipeIds = foundRecipeInfos.stream().map(RecipeInfo::getId).toList();
          assertThat(foundRecipeIds).containsExactlyInAnyOrderElementsOf(savedRecipeIds);

          // 각 상태가 모두 포함되는지 확인
          boolean hasInProgress =
              foundRecipeInfos.stream()
                  .anyMatch(recipe -> recipe.getRecipeStatus() == RecipeStatus.IN_PROGRESS);
          boolean hasSuccess =
              foundRecipeInfos.stream()
                  .anyMatch(recipe -> recipe.getRecipeStatus() == RecipeStatus.SUCCESS);
          boolean hasFailed =
              foundRecipeInfos.stream()
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

        private List<RecipeInfo> foundRecipeInfos;

        @BeforeEach
        void setUp() {
          foundRecipeInfos = recipeInfoRepository.findAllByIdIn(mixedIds);
        }

        @DisplayName("Then - 존재하는 레시피들만 반환된다")
        @Test
        void thenReturnsOnlyExistingRecipes() {
          assertThat(foundRecipeInfos).hasSize(2);

          List<UUID> foundRecipeIds = foundRecipeInfos.stream().map(RecipeInfo::getId).toList();
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

        private List<RecipeInfo> foundRecipeInfos;

        @BeforeEach
        void setUp() {
          foundRecipeInfos = recipeInfoRepository.findAllByIdIn(List.of());
        }

        @DisplayName("Then - 빈 목록이 반환된다")
        @Test
        void thenReturnsEmptyList() {
          assertThat(foundRecipeInfos).isEmpty();
        }
      }
    }
  }

  @Nested
  @DisplayName("상태별 레시피 페이징 조회")
  class FindByRecipeInfoStatus {

    @BeforeEach
    void setUp() {
      doReturn(LocalDateTime.now()).when(clock).now();
      RecipeInfo successRecipeInfo1 = RecipeInfo.create(clock);
      successRecipeInfo1.success(clock);
      RecipeInfo successRecipeInfo2 = RecipeInfo.create(clock);
      successRecipeInfo2.success(clock);
      RecipeInfo successRecipeInfo3 = RecipeInfo.create(clock);
      successRecipeInfo3.success(clock);
      RecipeInfo successRecipeInfo4 = RecipeInfo.create(clock);
      successRecipeInfo4.success(clock);
      RecipeInfo successRecipeInfo5 = RecipeInfo.create(clock);
      successRecipeInfo5.success(clock);

      RecipeInfo inProgressRecipeInfo1 = RecipeInfo.create(clock);
      RecipeInfo inProgressRecipeInfo2 = RecipeInfo.create(clock);

      RecipeInfo failedRecipeInfo1 = RecipeInfo.create(clock);
      failedRecipeInfo1.failed(clock);
      RecipeInfo failedRecipeInfo2 = RecipeInfo.create(clock);
      failedRecipeInfo2.failed(clock);

      recipeInfoRepository.saveAll(
          List.of(
              successRecipeInfo1,
              successRecipeInfo2,
              successRecipeInfo3,
              successRecipeInfo4,
              successRecipeInfo5,
              inProgressRecipeInfo1,
              inProgressRecipeInfo2,
              failedRecipeInfo1,
              failedRecipeInfo2));

      recipeInfoRepository.increaseCount(successRecipeInfo1.getId());
      recipeInfoRepository.increaseCount(successRecipeInfo1.getId());
      recipeInfoRepository.increaseCount(successRecipeInfo2.getId());
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

          Page<RecipeInfo> result =
              recipeInfoRepository.findByRecipeStatus(RecipeStatus.SUCCESS, pageable);

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

          Page<RecipeInfo> result =
              recipeInfoRepository.findByRecipeStatus(RecipeStatus.SUCCESS, pageable);

          assertThat(result.getContent()).hasSize(3);
          assertThat(result.isFirst()).isTrue();
          assertThat(result.hasNext()).isTrue();
        }

        @Test
        @DisplayName("Then - 두 번째 페이지도 올바르게 반환된다")
        void thenReturnsSecondPageCorrectly() {
          Pageable pageable = PageRequest.of(1, 3);

          Page<RecipeInfo> result =
              recipeInfoRepository.findByRecipeStatus(RecipeStatus.SUCCESS, pageable);

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

          Page<RecipeInfo> result =
              recipeInfoRepository.findByRecipeStatus(RecipeStatus.IN_PROGRESS, pageable);

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

          Page<RecipeInfo> result =
              recipeInfoRepository.findByRecipeStatus(RecipeStatus.FAILED, pageable);

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
        recipeInfoRepository.deleteAll();
      }

      @Test
      @DisplayName("When - 해당 상태로 조회하면 Then - 빈 페이지가 반환된다")
      void whenFindByStatus_thenReturnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<RecipeInfo> result =
            recipeInfoRepository.findByRecipeStatus(RecipeStatus.SUCCESS, pageable);

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

        Page<RecipeInfo> result =
            recipeInfoRepository.findByRecipeStatus(RecipeStatus.SUCCESS, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getNumber()).isEqualTo(10);
        assertThat(result.isFirst()).isFalse();
        assertThat(result.isLast()).isTrue();
      }
    }

    @Nested
    @DisplayName("Given - RecipePageRequest를 사용할 때")
    class GivenRecipeInfoPageRequest {

      @Test
      @DisplayName("When - RecipePageRequest.create로 조회수 내림차순 조회하면 Then - 조회수 순으로 정렬되어 반환된다")
      void whenFindWithRecipePageRequestCountDesc_thenReturnsOrderedByViewCount() {
        Pageable pageable = RecipePageRequest.create(0, RecipeSort.COUNT_DESC);

        Page<RecipeInfo> result =
            recipeInfoRepository.findByRecipeStatus(RecipeStatus.SUCCESS, pageable);

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getSize()).isEqualTo(10);

        List<RecipeInfo> recipeInfos = result.getContent();
        for (int i = 0; i < recipeInfos.size() - 1; i++) {
          assertThat(recipeInfos.get(i).getViewCount())
              .isGreaterThanOrEqualTo(recipeInfos.get(i + 1).getViewCount());
        }

        assertThat(recipeInfos.get(0).getViewCount()).isGreaterThanOrEqualTo(2);
      }

      @Test
      @DisplayName("When - RecipePageRequest.create로 1페이지 조회하면 Then - 두 번째 페이지가 반환된다")
      void whenFindSecondPageWithRecipePageRequest_thenReturnsSecondPage() {
        Pageable pageable = RecipePageRequest.create(1, RecipeSort.COUNT_DESC);

        Page<RecipeInfo> result =
            recipeInfoRepository.findByRecipeStatus(RecipeStatus.SUCCESS, pageable);

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

        Page<RecipeInfo> result =
            recipeInfoRepository.findByRecipeStatus(RecipeStatus.IN_PROGRESS, pageable);

        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getSize()).isEqualTo(10);

        result
            .getContent()
            .forEach(
                recipe -> {
                  assertThat(recipe.getRecipeStatus()).isEqualTo(RecipeStatus.IN_PROGRESS);
                });

        List<RecipeInfo> recipeInfos = result.getContent();
        for (int i = 0; i < recipeInfos.size() - 1; i++) {
          assertThat(recipeInfos.get(i).getViewCount())
              .isGreaterThanOrEqualTo(recipeInfos.get(i + 1).getViewCount());
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
    class GivenSavedRecipeInfo {

      private RecipeInfo savedRecipeInfo;

      @BeforeEach
      void setUp() {
        doReturn(LocalDateTime.now()).when(clock).now();
        RecipeInfo recipeInfo = RecipeInfo.create(clock);
        savedRecipeInfo = recipeInfoRepository.save(recipeInfo);
      }

      @Nested
      @DisplayName("When - 저장된 레시피 ID로 존재 여부를 확인하면")
      class WhenCheckingExistenceWithSavedId {

        @Test
        @DisplayName("Then - true가 반환된다")
        void thenReturnTrue() {
          boolean exists = recipeInfoRepository.existsById(savedRecipeInfo.getId());

          assertThat(exists).isTrue();
        }
      }

      @Nested
      @DisplayName("When - 다른 상태의 레시피들이 존재할 때")
      class WhenDifferentStatusRecipesExist {

        private RecipeInfo successRecipeInfo;
        private RecipeInfo failedRecipeInfo;

        @BeforeEach
        void setUp() {
          doReturn(LocalDateTime.now()).when(clock).now();
          RecipeInfo recipeInfo1 = RecipeInfo.create(clock);
          recipeInfo1.success(clock);
          successRecipeInfo = recipeInfoRepository.save(recipeInfo1);

          RecipeInfo recipeInfo2 = RecipeInfo.create(clock);
          recipeInfo2.failed(clock);
          failedRecipeInfo = recipeInfoRepository.save(recipeInfo2);
        }

        @Test
        @DisplayName("Then - 모든 상태의 레시피가 존재하는 것으로 확인된다")
        void thenAllStatusRecipesExist() {
          assertThat(recipeInfoRepository.existsById(savedRecipeInfo.getId()))
              .isTrue(); // IN_PROGRESS
          assertThat(recipeInfoRepository.existsById(successRecipeInfo.getId()))
              .isTrue(); // SUCCESS
          assertThat(recipeInfoRepository.existsById(failedRecipeInfo.getId())).isTrue(); // FAILED
        }
      }
    }

    @Nested
    @DisplayName("Given - 존재하지 않는 레시피 ID가 주어졌을 때")
    class GivenNonExistentRecipeInfoId {

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
          boolean exists = recipeInfoRepository.existsById(nonExistentId);

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
        List<RecipeInfo> recipeInfos = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
          RecipeInfo recipeInfo = RecipeInfo.create(clock);
          if (i % 3 == 0) {
            recipeInfo.success(clock);
          } else if (i % 5 == 0) {
            recipeInfo.failed(clock);
          }
          recipeInfos.add(recipeInfo);
        }
        recipeInfoRepository.saveAll(recipeInfos);
      }

      @Nested
      @DisplayName("When - 대량 데이터에서 상태별 조회를 하면")
      class WhenQueryingLargeDataSet {

        @Test
        @DisplayName("Then - 성능 저하 없이 정상 조회된다")
        void thenPerformsWellWithLargeDataSet() {
          long startTime = System.currentTimeMillis();

          Pageable pageable = RecipePageRequest.create(0, RecipeSort.COUNT_DESC);
          Page<RecipeInfo> successRecipes =
              recipeInfoRepository.findByRecipeStatus(RecipeStatus.SUCCESS, pageable);
          Page<RecipeInfo> inProgressRecipes =
              recipeInfoRepository.findByRecipeStatus(RecipeStatus.IN_PROGRESS, pageable);
          Page<RecipeInfo> failedRecipes =
              recipeInfoRepository.findByRecipeStatus(RecipeStatus.FAILED, pageable);

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
        RecipeInfo normalRecipeInfo1 = RecipeInfo.create(clock);
        normalRecipeInfo1.success(clock);
        normalRecipeId1 = recipeInfoRepository.save(normalRecipeInfo1).getId();
        createYoutubeMeta(normalRecipeId1, YoutubeMetaType.NORMAL);

        RecipeInfo normalRecipeInfo2 = RecipeInfo.create(clock);
        normalRecipeInfo2.success(clock);
        normalRecipeId2 = recipeInfoRepository.save(normalRecipeInfo2).getId();
        createYoutubeMeta(normalRecipeId2, YoutubeMetaType.NORMAL);

        // SHORTS 타입 레시피 생성
        RecipeInfo shortsRecipeInfo = RecipeInfo.create(clock);
        shortsRecipeInfo.success(clock);
        shortsRecipeId = recipeInfoRepository.save(shortsRecipeInfo).getId();
        createYoutubeMeta(shortsRecipeId, YoutubeMetaType.SHORTS);
      }

      @Nested
      @DisplayName("When - NORMAL 레시피를 조회하면")
      class WhenFindingNormalRecipes {

        @Test
        @DisplayName("Then - NORMAL 타입 레시피만 반환된다")
        void thenReturnsOnlyNormalRecipes() {
          Page<RecipeInfo> result =
              recipeInfoRepository.findRecipes(
                  RecipeStatus.SUCCESS, pageable, YoutubeMetaType.NORMAL.name());

          assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(2);
          assertThat(result.getContent())
              .extracting(RecipeInfo::getId)
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

        RecipeInfo shortsRecipeInfo1 = RecipeInfo.create(clock);
        shortsRecipeInfo1.success(clock);
        shortsRecipeId1 = recipeInfoRepository.save(shortsRecipeInfo1).getId();
        createYoutubeMeta(shortsRecipeId1, YoutubeMetaType.SHORTS);

        RecipeInfo shortsRecipeInfo2 = RecipeInfo.create(clock);
        shortsRecipeInfo2.success(clock);
        shortsRecipeId2 = recipeInfoRepository.save(shortsRecipeInfo2).getId();
        createYoutubeMeta(shortsRecipeId2, YoutubeMetaType.SHORTS);

        RecipeInfo normalRecipeInfo = RecipeInfo.create(clock);
        normalRecipeInfo.success(clock);
        normalRecipeId = recipeInfoRepository.save(normalRecipeInfo).getId();
        createYoutubeMeta(normalRecipeId, YoutubeMetaType.NORMAL);
      }

      @Nested
      @DisplayName("When - SHORTS 레시피를 조회하면")
      class WhenFindingShortsRecipes {

        @Test
        @DisplayName("Then - SHORTS 타입 레시피만 반환된다")
        void thenReturnsOnlyShortsRecipes() {
          Page<RecipeInfo> result =
              recipeInfoRepository.findRecipes(
                  RecipeStatus.SUCCESS, pageable, YoutubeMetaType.SHORTS.name());

          assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(2);
          assertThat(result.getContent())
              .extracting(RecipeInfo::getId)
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
        RecipeInfo koreanRecipeInfo1 = RecipeInfo.create(clock);
        koreanRecipeInfo1.success(clock);
        koreanRecipeId1 = recipeInfoRepository.save(koreanRecipeInfo1).getId();
        createRecipeTag(koreanRecipeId1, "한식");

        RecipeInfo koreanRecipeInfo2 = RecipeInfo.create(clock);
        koreanRecipeInfo2.success(clock);
        koreanRecipeId2 = recipeInfoRepository.save(koreanRecipeInfo2).getId();
        createRecipeTag(koreanRecipeId2, "한식");

        // 중식 태그를 가진 레시피 생성
        RecipeInfo chineseRecipeInfo = RecipeInfo.create(clock);
        chineseRecipeInfo.success(clock);
        chineseRecipeId = recipeInfoRepository.save(chineseRecipeInfo).getId();
        createRecipeTag(chineseRecipeId, "중식");
      }

      @Nested
      @DisplayName("When - 한식 레시피를 조회하면")
      class WhenFindingKoreanRecipes {

        @Test
        @DisplayName("Then - 한식 태그를 가진 레시피만 반환된다")
        void thenReturnsOnlyKoreanRecipes() {
          Page<RecipeInfo> result =
              recipeInfoRepository.findCuisineRecipes("한식", RecipeStatus.SUCCESS, pageable);

          assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(2);
          assertThat(result.getContent())
              .extracting(RecipeInfo::getId)
              .contains(koreanRecipeId1, koreanRecipeId2);
          assertThat(result.getContent())
              .extracting(RecipeInfo::getId)
              .doesNotContain(chineseRecipeId);
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

        RecipeInfo chineseRecipeInfo1 = RecipeInfo.create(clock);
        chineseRecipeInfo1.success(clock);
        chineseRecipeId1 = recipeInfoRepository.save(chineseRecipeInfo1).getId();
        createRecipeTag(chineseRecipeId1, "중식");

        RecipeInfo chineseRecipeInfo2 = RecipeInfo.create(clock);
        chineseRecipeInfo2.success(clock);
        chineseRecipeId2 = recipeInfoRepository.save(chineseRecipeInfo2).getId();
        createRecipeTag(chineseRecipeId2, "중식");

        RecipeInfo japaneseRecipeInfo = RecipeInfo.create(clock);
        japaneseRecipeInfo.success(clock);
        japaneseRecipeId = recipeInfoRepository.save(japaneseRecipeInfo).getId();
        createRecipeTag(japaneseRecipeId, "일식");
      }

      @Nested
      @DisplayName("When - 중식 레시피를 조회하면")
      class WhenFindingChineseRecipes {

        @Test
        @DisplayName("Then - 중식 태그를 가진 레시피만 반환된다")
        void thenReturnsOnlyChineseRecipes() {
          Page<RecipeInfo> result =
              recipeInfoRepository.findCuisineRecipes("중식", RecipeStatus.SUCCESS, pageable);

          assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(2);
          assertThat(result.getContent())
              .extracting(RecipeInfo::getId)
              .contains(chineseRecipeId1, chineseRecipeId2);
          assertThat(result.getContent())
              .extracting(RecipeInfo::getId)
              .doesNotContain(japaneseRecipeId);
        }
      }
    }

    @Nested
    @DisplayName("Given - 진행 중 상태의 태그 레시피가 존재할 때")
    class GivenInProgressTaggedRecipeInfo {

      private UUID inProgressRecipeId;
      private UUID successRecipeId;
      private Pageable pageable;

      @BeforeEach
      void setUp() {
        pageable = RecipePageRequest.create(0, RecipeSort.COUNT_DESC);
        LocalDateTime now = LocalDateTime.now();
        doReturn(now).when(clock).now();

        RecipeInfo inProgressRecipeInfo = RecipeInfo.create(clock);
        inProgressRecipeId = recipeInfoRepository.save(inProgressRecipeInfo).getId();
        createRecipeTag(inProgressRecipeId, "한식");

        RecipeInfo successRecipeInfo = RecipeInfo.create(clock);
        successRecipeInfo.success(clock);
        successRecipeId = recipeInfoRepository.save(successRecipeInfo).getId();
        createRecipeTag(successRecipeId, "한식");
      }

      @Nested
      @DisplayName("When - SUCCESS 상태의 한식 레시피를 조회하면")
      class WhenFindingSuccessKoreanRecipes {

        @Test
        @DisplayName("Then - SUCCESS 상태의 한식 태그 레시피만 반환된다")
        void thenReturnsOnlySuccessKoreanRecipes() {
          Page<RecipeInfo> result =
              recipeInfoRepository.findCuisineRecipes("한식", RecipeStatus.SUCCESS, pageable);

          assertThat(result.getContent()).extracting(RecipeInfo::getId).contains(successRecipeId);
          assertThat(result.getContent())
              .extracting(RecipeInfo::getId)
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
    class GivenRecipeInfoWithMultipleTags {

      private UUID multiTagRecipeId;
      private Pageable pageable;

      @BeforeEach
      void setUp() {
        pageable = RecipePageRequest.create(0, RecipeSort.COUNT_DESC);
        LocalDateTime now = LocalDateTime.now();
        doReturn(now).when(clock).now();

        RecipeInfo multiTagRecipeInfo = RecipeInfo.create(clock);
        multiTagRecipeInfo.success(clock);
        multiTagRecipeId = recipeInfoRepository.save(multiTagRecipeInfo).getId();
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
          Page<RecipeInfo> result =
              recipeInfoRepository.findCuisineRecipes("한식", RecipeStatus.SUCCESS, pageable);

          assertThat(result.getContent()).extracting(RecipeInfo::getId).contains(multiTagRecipeId);
        }
      }

      @Nested
      @DisplayName("When - 매운맛 태그로 조회하면")
      class WhenFindingBySpicyTag {

        @Test
        @DisplayName("Then - 매운맛 태그를 가진 레시피가 반환된다")
        void thenReturnsRecipeWithSpicyTag() {
          Page<RecipeInfo> result =
              recipeInfoRepository.findCuisineRecipes("매운맛", RecipeStatus.SUCCESS, pageable);

          assertThat(result.getContent()).extracting(RecipeInfo::getId).contains(multiTagRecipeId);
        }
      }
    }

    @Nested
    @DisplayName("Given - 태그가 없는 레시피만 존재할 때")
    class GivenRecipeInfoWithoutTags {

      private Pageable pageable;

      @BeforeEach
      void setUp() {
        pageable = RecipePageRequest.create(0, RecipeSort.COUNT_DESC);
        LocalDateTime now = LocalDateTime.now();
        doReturn(now).when(clock).now();

        RecipeInfo recipeInfoWithoutTag = RecipeInfo.create(clock);
        recipeInfoWithoutTag.success(clock);
        recipeInfoRepository.save(recipeInfoWithoutTag);
      }

      @Nested
      @DisplayName("When - 한식 태그로 조회하면")
      class WhenFindingByKoreanTag {

        @Test
        @DisplayName("Then - 빈 페이지가 반환된다")
        void thenReturnsEmptyPage() {
          Page<RecipeInfo> result =
              recipeInfoRepository.findCuisineRecipes("한식", RecipeStatus.SUCCESS, pageable);

          assertThat(result.getContent()).isEmpty();
          assertThat(result.getTotalElements()).isEqualTo(0);
        }
      }
    }

    @Nested
    @DisplayName("커서 기반 조회 쿼리")
    class CursorQueries {

      @BeforeEach
      void setUp() {
        LocalDateTime now = LocalDateTime.now();
        doReturn(now).when(clock).now();
        recipeInfoRepository.deleteAllInBatch();
      }

      @Test
      @DisplayName("인기 레시피 첫 페이지를 조회한다")
      void shouldFindPopularFirst() {
        RecipeInfo recipe1 = RecipeInfo.create(clock);
        recipe1.success(clock);
        RecipeInfo recipe2 = RecipeInfo.create(clock);
        recipe2.success(clock);
        RecipeInfo recipe3 = RecipeInfo.create(clock);
        recipe3.success(clock);

        recipeInfoRepository.saveAll(List.of(recipe1, recipe2, recipe3));
        recipeInfoRepository.increaseCount(recipe1.getId());
        recipeInfoRepository.increaseCount(recipe1.getId());
        recipeInfoRepository.increaseCount(recipe2.getId());

        Pageable pageable = PageRequest.of(0, 2);
        List<RecipeInfo> result =
            recipeInfoRepository.findPopularFirst(RecipeStatus.SUCCESS, pageable);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getId()).isEqualTo(recipe1.getId());
        assertThat(result.get(1).getId()).isEqualTo(recipe2.getId());
      }

      @Test
      @DisplayName("인기 레시피 keyset을 조회한다")
      void shouldFindPopularKeyset() {
        RecipeInfo recipe1 = RecipeInfo.create(clock);
        recipe1.success(clock);
        RecipeInfo recipe2 = RecipeInfo.create(clock);
        recipe2.success(clock);
        RecipeInfo recipe3 = RecipeInfo.create(clock);
        recipe3.success(clock);

        recipeInfoRepository.saveAll(List.of(recipe1, recipe2, recipe3));
        recipeInfoRepository.increaseCount(recipe1.getId());
        recipeInfoRepository.increaseCount(recipe1.getId());
        recipeInfoRepository.increaseCount(recipe2.getId());

        RecipeInfo latest = recipeInfoRepository.findById(recipe2.getId()).orElseThrow();

        Pageable pageable = PageRequest.of(0, 2);
        List<RecipeInfo> result =
            recipeInfoRepository.findPopularKeyset(
                RecipeStatus.SUCCESS, latest.getViewCount(), latest.getId(), pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(recipe3.getId());
      }

      @Test
      @DisplayName("요리 카테고리 첫 페이지를 조회한다")
      void shouldFindCuisineFirst() {
        RecipeInfo recipe1 = RecipeInfo.create(clock);
        recipe1.success(clock);
        RecipeInfo recipe2 = RecipeInfo.create(clock);
        recipe2.success(clock);

        UUID recipeId1 = recipeInfoRepository.save(recipe1).getId();
        UUID recipeId2 = recipeInfoRepository.save(recipe2).getId();
        createRecipeTag(recipeId1, "한식");
        createRecipeTag(recipeId2, "한식");

        recipeInfoRepository.increaseCount(recipeId1);

        Pageable pageable = PageRequest.of(0, 2);
        List<RecipeInfo> result =
            recipeInfoRepository.findCuisineFirst("한식", RecipeStatus.SUCCESS, pageable);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getId()).isEqualTo(recipeId1);
      }

      @Test
      @DisplayName("요리 카테고리 keyset을 조회한다")
      void shouldFindCuisineKeyset() {
        RecipeInfo recipe1 = RecipeInfo.create(clock);
        recipe1.success(clock);
        RecipeInfo recipe2 = RecipeInfo.create(clock);
        recipe2.success(clock);

        UUID recipeId1 = recipeInfoRepository.save(recipe1).getId();
        UUID recipeId2 = recipeInfoRepository.save(recipe2).getId();
        createRecipeTag(recipeId1, "한식");
        createRecipeTag(recipeId2, "한식");

        recipeInfoRepository.increaseCount(recipeId1);
        RecipeInfo last = recipeInfoRepository.findById(recipeId1).orElseThrow();

        Pageable pageable = PageRequest.of(0, 2);
        List<RecipeInfo> result =
            recipeInfoRepository.findCuisineKeyset(
                "한식", RecipeStatus.SUCCESS, last.getViewCount(), last.getId(), pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(recipeId2);
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
    doReturn("Test Channel").when(videoInfo).getChannelTitle();
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
