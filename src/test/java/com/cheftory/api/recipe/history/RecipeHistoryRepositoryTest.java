package com.cheftory.api.recipe.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.history.entity.RecipeHistory;
import com.cheftory.api.recipe.history.entity.RecipeHistoryCategorizedCountProjection;
import com.cheftory.api.recipe.history.entity.RecipeHistoryStatus;
import com.cheftory.api.recipe.history.entity.RecipeHistoryUnCategorizedCountProjection;
import com.cheftory.api.recipe.history.utils.RecipeHistorySort;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DisplayName("RecipeHistoryRepository Tests")
public class RecipeHistoryRepositoryTest extends DbContextTest {

  @Autowired private RecipeHistoryRepository repository;
  @MockitoBean private Clock clock;

  @BeforeEach
  void setUp() {}

  @Nested
  @DisplayName("레시피 조회 상태 저장")
  class SaveRecipeHistory {

    @Nested
    @DisplayName("Given - 유효한 레시피 조회 상태가 주어졌을 때")
    class GivenValidRecipeHistory {

      private UUID recipeId;
      private UUID userId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        userId = UUID.randomUUID();
        when(clock.now()).thenReturn(LocalDateTime.now());
      }

      @Nested
      @DisplayName("When - 레시피 조회 상태를 저장한다면")
      class WhenSavingRecipeHistory {

        private RecipeHistory recipeHistory;

        @BeforeEach
        void beforeEach() {
          recipeHistory = RecipeHistory.create(clock, userId, recipeId);
          repository.save(recipeHistory);
        }

        @DisplayName("Then - 레시피 조회 상태가 저장되어야 한다")
        @Test
        void thenShouldSaveRecipeHistory() {
          RecipeHistory savedStatus = repository.findById(recipeHistory.getId()).orElseThrow();
          assertThat(savedStatus.getRecipeId()).isEqualTo(recipeId);
          assertThat(savedStatus.getUserId()).isEqualTo(userId);
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 조회 상태 존재 확인")
  class ExistsByRecipeIdAndUserId {

    @Nested
    @DisplayName("Given - 유효한 레시피 ID와 사용자 ID가 주어졌을 때")
    class GivenValidRecipeIdAndUserId {

      private UUID recipeId;
      private UUID userId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        userId = UUID.randomUUID();
        when(clock.now()).thenReturn(LocalDateTime.now());
      }

      @Nested
      @DisplayName("When - 레시피 조회 상태를 확인한다면")
      class WhenCheckingExists {

        private RecipeHistory recipeHistory;

        @BeforeEach
        void beforeEach() {
          recipeHistory = RecipeHistory.create(clock, userId, recipeId);
          repository.save(recipeHistory);
        }

        @Test
        @DisplayName("Then - 해당 레시피 조회 상태가 존재해야 한다")
        void thenShouldExist() {
          boolean exists =
              repository.existsByRecipeIdAndUserIdAndStatus(
                  recipeId, userId, RecipeHistoryStatus.ACTIVE);
          assertThat(exists).isTrue();
        }
      }
    }

    @Nested
    @DisplayName("Given - 존재하지 않는 레시피 ID와 사용자 ID가 주어졌을 때")
    class GivenNonExistentRecipeIdAndUserId {

      private UUID recipeId;
      private UUID userId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        userId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 레시피 조회 상태를 확인한다면")
      class WhenCheckingExists {

        @Test
        @DisplayName("Then - 해당 레시피 조회 상태가 존재하지 않아야 한다")
        void thenShouldNotExist() {
          boolean exists =
              repository.existsByRecipeIdAndUserIdAndStatus(
                  recipeId, userId, RecipeHistoryStatus.ACTIVE);
          assertThat(exists).isFalse();
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 조회 상태 가져오기")
  class FindByRecipeIdAndUserId {

    @Nested
    @DisplayName("Given - 유효한 레시피 ID와 사용자 ID가 주어졌을 때")
    class GivenValidRecipeIdAndUserId {

      private UUID recipeId;
      private UUID userId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        userId = UUID.randomUUID();
        when(clock.now()).thenReturn(LocalDateTime.now());
      }

      @Nested
      @DisplayName("When - 레시피 조회 상태를 가져온다면")
      class WhenFindingByRecipeIdAndUserId {

        private RecipeHistory recipeHistory;

        @BeforeEach
        void beforeEach() {
          recipeHistory = RecipeHistory.create(clock, userId, recipeId);
          repository.save(recipeHistory);
        }

        @Test
        @DisplayName("Then - 해당 레시피 조회 상태를 반환해야 한다")
        void thenShouldReturnRecipeHistory() {
          RecipeHistory foundStatus =
              repository
                  .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeHistoryStatus.ACTIVE)
                  .orElseThrow();
          assertThat(foundStatus.getRecipeId()).isEqualTo(recipeId);
          assertThat(foundStatus.getUserId()).isEqualTo(userId);
        }
      }
    }

    @Nested
    @DisplayName("Given - 존재하지 않는 레시피 ID와 사용자 ID가 주어졌을 때")
    class GivenNonExistentRecipeIdAndUserId {

      private UUID recipeId;
      private UUID userId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        userId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 레시피 조회 상태를 가져온다면")
      class WhenFindingByRecipeIdAndUserId {

        @Test
        @DisplayName("Then - 빈 결과를 반환해야 한다")
        void thenShouldReturnEmpty() {
          assertThat(
                  repository.findByRecipeIdAndUserIdAndStatus(
                      recipeId, userId, RecipeHistoryStatus.ACTIVE))
              .isEmpty();
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 조회 상태 미분류 카테고리 조회")
  class FindUncategorizedRecipeHistoryes {

    @Nested
    @DisplayName("Given - 미분류 레시피 조회 상태가 존재할 때")
    class GivenUncategorizedRecipeHistoryes {

      private UUID userId;
      private UUID recipeId;
      private Integer page;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        recipeId = UUID.randomUUID();
        page = 0;
        when(clock.now()).thenReturn(LocalDateTime.now());
      }

      @Nested
      @DisplayName("When - 미분류 레시피 조회 상태를 가져온다면")
      class WhenFindingUncategorized {

        private RecipeHistory recipeHistory;

        @BeforeEach
        void beforeEach() {
          recipeHistory = RecipeHistory.create(clock, userId, recipeId);
          repository.save(recipeHistory);
        }

        @Test
        @DisplayName("Then - 해당 레시피 조회 상태를 반환해야 한다")
        void thenShouldReturnUncategorizedRecipeHistoryes() {

          Pageable pageable = PageRequest.of(page, 10);
          var statuses =
              repository
                  .findAllByUserIdAndRecipeCategoryIdAndStatus(
                      userId, null, RecipeHistoryStatus.ACTIVE, pageable)
                  .getContent();

          assertThat(statuses.size()).isEqualTo(1);
          assertThat(statuses.getFirst().getUserId()).isEqualTo(userId);
          assertThat(statuses.getFirst().getRecipeId()).isEqualTo(recipeId);
          assertThat(statuses.getFirst().getRecipeCategoryId()).isNull();
        }
      }
    }

    @Nested
    @DisplayName("Given - 미분류 레시피 조회 상태가 존재하지 않을 때")
    class GivenNoUncategorizedRecipeHistoryes {

      private UUID userId;
      private Integer page;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        page = 0;
      }

      @Nested
      @DisplayName("When - 미분류 레시피 조회 상태를 가져온다면")
      class WhenFindingUncategorized {

        @Test
        @DisplayName("Then - 빈 결과를 반환해야 한다")
        void thenShouldReturnEmpty() {
          Pageable pageable = PageRequest.of(page, 10);
          var statuses =
              repository
                  .findAllByUserIdAndRecipeCategoryIdAndStatus(
                      userId, null, RecipeHistoryStatus.ACTIVE, pageable)
                  .getContent();
          assertThat(statuses.size()).isEqualTo(0);
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 조회 상태 분류 카테고리 조회")
  class FindCategorizedRecipeHistoryes {

    @Nested
    @DisplayName("Given - 분류된 레시피 조회 상태가 존재할 때")
    class GivenCategorizedRecipeHistoryes {

      private UUID userId;
      private UUID recipeId;
      private UUID categoryId;
      private Integer page;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        recipeId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        page = 0;
        when(clock.now()).thenReturn(LocalDateTime.now());
      }

      @Nested
      @DisplayName("When - 분류된 레시피 조회 상태를 가져온다면")
      class WhenFindingCategorized {

        private RecipeHistory recipeHistory;

        @BeforeEach
        void beforeEach() {
          recipeHistory = RecipeHistory.create(clock, userId, recipeId);
          recipeHistory.updateRecipeCategoryId(categoryId);
          repository.save(recipeHistory);
        }

        @Test
        @DisplayName("Then - 해당 레시피 조회 상태를 반환해야 한다")
        void thenShouldReturnCategorizedRecipeHistoryes() {

          Pageable pageable = PageRequest.of(page, 10);
          var statuses =
              repository
                  .findAllByUserIdAndRecipeCategoryIdAndStatus(
                      userId, categoryId, RecipeHistoryStatus.ACTIVE, pageable)
                  .getContent();

          assertThat(statuses.size()).isEqualTo(1);
          assertThat(statuses.getFirst().getUserId()).isEqualTo(userId);
          assertThat(statuses.getFirst().getRecipeId()).isEqualTo(recipeId);
          assertThat(statuses.getFirst().getRecipeCategoryId()).isEqualTo(categoryId);
        }
      }
    }

    @Nested
    @DisplayName("Given - 분류된 레시피 조회 상태가 존재하지 않을 때")
    class GivenNoCategorizedRecipeHistoryes {

      private UUID userId;
      private UUID categoryId;
      private Integer page;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        page = 0;
      }

      @Nested
      @DisplayName("When - 분류된 레시피 조회 상태를 가져온다면")
      class WhenFindingCategorized {

        @Test
        @DisplayName("Then - 빈 결과를 반환해야 한다")
        void thenShouldReturnEmpty() {
          Pageable pageable = PageRequest.of(page, 10);
          var statuses =
              repository
                  .findAllByUserIdAndRecipeCategoryIdAndStatus(
                      userId, categoryId, RecipeHistoryStatus.ACTIVE, pageable)
                  .getContent();
          assertThat(statuses.size()).isEqualTo(0);
        }
      }
    }
  }

  @Nested
  @DisplayName("사용자의 최근 조회 레시피 상태 조회")
  class FindRecentRecipeHistoryesByUserId {

    @Nested
    @DisplayName("Given - 유효한 사용자 ID가 주어졌을 때")
    class GivenValidUserId {

      private UUID userId;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 사용자의 최근 조회 레시피를 가져온다면")
      class WhenFindingRecentRecipeHistoryesByUserId {

        private UUID firstRecipeId;
        private UUID secondRecipeId;
        private RecipeHistory firstRecipeHistory;
        private RecipeHistory secondRecipeHistory;

        @BeforeEach
        void beforeEach() {
          firstRecipeId = UUID.randomUUID();
          secondRecipeId = UUID.randomUUID();

          // 첫 번째는 더 이전 시간으로 설정
          LocalDateTime firstTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
          when(clock.now()).thenReturn(firstTime);
          firstRecipeHistory = RecipeHistory.create(clock, userId, firstRecipeId);
          repository.save(firstRecipeHistory);

          // 두 번째는 더 최근 시간으로 설정
          LocalDateTime secondTime = LocalDateTime.of(2024, 1, 1, 11, 0, 0);
          when(clock.now()).thenReturn(secondTime);
          secondRecipeHistory = RecipeHistory.create(clock, userId, secondRecipeId);
          repository.save(secondRecipeHistory);
        }

        @Test
        @DisplayName("Then - 최근 조회 순서로 정렬된 레시피 상태들을 반환해야 한다")
        void thenShouldReturnRecentRecipeHistoryesByUserId() {
          Pageable pageable = PageRequest.of(0, 10, RecipeHistorySort.VIEWED_AT_DESC);
          Page<RecipeHistory> page =
              repository.findByUserIdAndStatus(userId, RecipeHistoryStatus.ACTIVE, pageable);
          List<RecipeHistory> statuses = page.getContent();

          assertThat(statuses).hasSize(2);
          assertThat(statuses.get(0).getUserId()).isEqualTo(userId);

          // 더 최근 것(11시)이 먼저 나와야 함
          assertThat(statuses.get(0).getRecipeId()).isEqualTo(secondRecipeHistory.getRecipeId());
          assertThat(statuses.get(1).getRecipeId()).isEqualTo(firstRecipeHistory.getRecipeId());

          // 시간 검증 - 첫 번째가 더 최근이어야 함
          assertThat(statuses.get(0).getViewedAt()).isAfter(statuses.get(1).getViewedAt());
        }
      }

      @Nested
      @DisplayName("When - 사용자의 10개의 최근 조회 레시피를 가져온다면")
      class WhenFindinTengRecentRecipeHistoryesByUserId {

        private List<UUID> recipeIds;
        private List<RecipeHistory> recipeHistories;
        private LocalDateTime baseTime;

        @BeforeEach
        void beforeEach() {
          recipeIds = new ArrayList<>();
          recipeHistories = new ArrayList<>();
          baseTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);

          // 20개의 레시피 조회 상태 생성 (시간 순서대로)
          for (int i = 0; i < 20; i++) {
            UUID recipeId = UUID.randomUUID();
            recipeIds.add(recipeId);

            // 각각 1분씩 차이나게 설정 (i=0이 가장 오래된 것)
            LocalDateTime viewTime = baseTime.plusMinutes(i);
            when(clock.now()).thenReturn(viewTime);

            RecipeHistory viewStatus = RecipeHistory.create(clock, userId, recipeId);
            recipeHistories.add(viewStatus);
            repository.save(viewStatus);
          }
        }

        @Test
        @DisplayName("Then - 최근 조회 순서로 정렬된 레시피 상태들을 반환해야 한다")
        void thenShouldReturnRecentRecipeHistoryesByUserId() {
          // Given
          Pageable pageable = PageRequest.of(0, 10, RecipeHistorySort.VIEWED_AT_DESC);

          // When
          Page<RecipeHistory> statusPage =
              repository.findByUserIdAndStatus(userId, RecipeHistoryStatus.ACTIVE, pageable);

          // Then
          assertThat(statusPage.getTotalElements()).isEqualTo(20);
          assertThat(statusPage.getContent()).hasSize(10); // 페이지 크기만큼만
          assertThat(statusPage.getNumber()).isEqualTo(0); // 현재 페이지
          assertThat(statusPage.getSize()).isEqualTo(10); // 페이지 크기
          assertThat(statusPage.getTotalPages()).isEqualTo(2); // 총 페이지 수

          List<RecipeHistory> statuses = statusPage.getContent();
          assertThat(statuses.get(0).getUserId()).isEqualTo(userId);

          // 가장 최근 것(19번 인덱스)이 첫 번째로 나와야 함
          assertThat(statuses.get(0).getRecipeId()).isEqualTo(recipeIds.get(19));
          assertThat(statuses.get(9).getRecipeId()).isEqualTo(recipeIds.get(10)); // 10번째는 인덱스 10

          // 시간 순서 검증 - 내림차순으로 정렬되어야 함
          for (int i = 0; i < statuses.size() - 1; i++) {
            assertThat(statuses.get(i).getViewedAt()).isAfter(statuses.get(i + 1).getViewedAt());
          }
        }

        @Test
        @DisplayName("Then - 페이지네이션이 올바르게 적용되어야 한다")
        void thenShouldApplyPaginationCorrectly() {
          // Given - 첫 번째 페이지 (페이지 크기 10)
          Pageable firstPageable = PageRequest.of(0, 10, RecipeHistorySort.VIEWED_AT_DESC);

          // When
          Page<RecipeHistory> firstPage =
              repository.findByUserIdAndStatus(userId, RecipeHistoryStatus.ACTIVE, firstPageable);

          // Then
          assertThat(firstPage.getTotalElements()).isEqualTo(20);
          assertThat(firstPage.getContent()).hasSize(10);
          assertThat(firstPage.getNumber()).isEqualTo(0);
          assertThat(firstPage.getTotalPages()).isEqualTo(2);
          assertThat(firstPage.hasNext()).isTrue();
          assertThat(firstPage.hasPrevious()).isFalse();

          // 가장 최근 10개가 첫 페이지에 나와야 함 (인덱스 19~10)
          List<RecipeHistory> firstPageContent = firstPage.getContent();
          assertThat(firstPageContent.get(0).getRecipeId()).isEqualTo(recipeIds.get(19)); // 가장 최근
          assertThat(firstPageContent.get(9).getRecipeId())
              .isEqualTo(recipeIds.get(10)); // 10번째로 최근

          // Given - 두 번째 페이지
          Pageable secondPageable = PageRequest.of(1, 10, RecipeHistorySort.VIEWED_AT_DESC);

          // When
          Page<RecipeHistory> secondPage =
              repository.findByUserIdAndStatus(userId, RecipeHistoryStatus.ACTIVE, secondPageable);

          // Then
          assertThat(secondPage.getTotalElements()).isEqualTo(20);
          assertThat(secondPage.getContent()).hasSize(10);
          assertThat(secondPage.getNumber()).isEqualTo(1);
          assertThat(secondPage.getTotalPages()).isEqualTo(2);
          assertThat(secondPage.hasNext()).isFalse();
          assertThat(secondPage.hasPrevious()).isTrue();

          // 나머지 10개가 두 번째 페이지에 나와야 함 (인덱스 9~0)
          List<RecipeHistory> secondPageContent = secondPage.getContent();
          assertThat(secondPageContent.get(0).getRecipeId())
              .isEqualTo(recipeIds.get(9)); // 11번째로 최근
          assertThat(secondPageContent.get(9).getRecipeId()).isEqualTo(recipeIds.get(0)); // 가장 오래된
        }

        @Test
        @DisplayName("Then - 빈 결과에 대해서도 올바른 페이지 정보를 반환해야 한다")
        void thenShouldReturnCorrectPageInfoForEmptyResult() {
          // Given
          UUID nonExistentUserId = UUID.randomUUID();
          Pageable pageable = PageRequest.of(0, 10, RecipeHistorySort.VIEWED_AT_DESC);

          // When
          Page<RecipeHistory> emptyPage =
              repository.findByUserIdAndStatus(
                  nonExistentUserId, RecipeHistoryStatus.ACTIVE, pageable);

          // Then
          assertThat(emptyPage.getTotalElements()).isEqualTo(0);
          assertThat(emptyPage.getContent()).isEmpty();
          assertThat(emptyPage.getNumber()).isEqualTo(0);
          assertThat(emptyPage.getTotalPages()).isEqualTo(0);
          assertThat(emptyPage.hasNext()).isFalse();
          assertThat(emptyPage.hasPrevious()).isFalse();
        }

        @Test
        @DisplayName("Then - 페이지 크기를 5로 설정했을 때 올바르게 동작해야 한다")
        void thenShouldWorkCorrectlyWithPageSizeFive() {
          // Given
          Pageable pageable = PageRequest.of(0, 5, RecipeHistorySort.VIEWED_AT_DESC);

          // When
          Page<RecipeHistory> page =
              repository.findByUserIdAndStatus(userId, RecipeHistoryStatus.ACTIVE, pageable);

          // Then
          assertThat(page.getTotalElements()).isEqualTo(20);
          assertThat(page.getContent()).hasSize(5);
          assertThat(page.getTotalPages()).isEqualTo(4); // 20 / 5 = 4페이지
          assertThat(page.hasNext()).isTrue();

          // 가장 최근 5개가 나와야 함 (인덱스 19~15)
          List<RecipeHistory> content = page.getContent();
          assertThat(content.get(0).getRecipeId()).isEqualTo(recipeIds.get(19));
          assertThat(content.get(4).getRecipeId()).isEqualTo(recipeIds.get(15));
        }
      }
    }

    @Nested
    @DisplayName("모든 레시피 히스토리 조회")
    class FindAllRecipeHistoriesByUserId {

      @Nested
      @DisplayName("Given - 유효한 사용자 ID가 주어졌을 때")
      class GivenValidUserId {

        private UUID userId;

        @BeforeEach
        void setUp() {
          userId = UUID.randomUUID();
          when(clock.now()).thenReturn(LocalDateTime.now());
        }

        @Nested
        @DisplayName("When - 사용자의 모든 레시피 히스토리를 조회한다면")
        class WhenFindingAllRecipeHistories {

          private List<RecipeHistory> allHistories;

          @BeforeEach
          void beforeEach() {
            allHistories = new ArrayList<>();

            // 카테고리가 있는 히스토리 2개
            for (int i = 0; i < 2; i++) {
              RecipeHistory history = RecipeHistory.create(clock, userId, UUID.randomUUID());
              history.updateRecipeCategoryId(UUID.randomUUID());
              repository.save(history);
              allHistories.add(history);
            }

            // 카테고리가 없는 히스토리 2개
            for (int i = 0; i < 2; i++) {
              RecipeHistory history = RecipeHistory.create(clock, userId, UUID.randomUUID());
              repository.save(history);
              allHistories.add(history);
            }
          }

          @Test
          @DisplayName("Then - 카테고리 유무와 상관없이 모든 히스토리를 반환해야 한다")
          void thenShouldReturnAllHistories() {
            Pageable pageable = PageRequest.of(0, 10, RecipeHistorySort.VIEWED_AT_DESC);
            Page<RecipeHistory> result =
                repository.findAllByUserIdAndStatus(userId, RecipeHistoryStatus.ACTIVE, pageable);

            assertThat(result.getContent()).hasSize(4);
            assertThat(result.getContent()).allMatch(h -> h.getUserId().equals(userId));

            // 카테고리가 있는 것과 없는 것이 모두 포함되어야 함
            long withCategory =
                result.getContent().stream().filter(h -> h.getRecipeCategoryId() != null).count();
            long withoutCategory =
                result.getContent().stream().filter(h -> h.getRecipeCategoryId() == null).count();

            assertThat(withCategory).isEqualTo(2);
            assertThat(withoutCategory).isEqualTo(2);
          }
        }
      }

      @Nested
      @DisplayName("Given - 카테고리가 있는 히스토리만 있을 때")
      class GivenOnlyCategorizedHistories {

        private UUID userId;
        private UUID categoryId;

        @BeforeEach
        void setUp() {
          userId = UUID.randomUUID();
          categoryId = UUID.randomUUID();
          when(clock.now()).thenReturn(LocalDateTime.now());

          // 모든 히스토리가 카테고리를 가지고 있음
          for (int i = 0; i < 3; i++) {
            RecipeHistory history = RecipeHistory.create(clock, userId, UUID.randomUUID());
            history.updateRecipeCategoryId(categoryId);
            repository.save(history);
          }
        }

        @Nested
        @DisplayName("When - 사용자의 모든 레시피 히스토리를 조회한다면")
        class WhenFindingAllRecipeHistories {

          @Test
          @DisplayName("Then - 카테고리가 있는 모든 히스토리를 반환해야 한다")
          void thenShouldReturnAllCategorizedHistories() {
            Pageable pageable = PageRequest.of(0, 10, RecipeHistorySort.VIEWED_AT_DESC);
            Page<RecipeHistory> result =
                repository.findAllByUserIdAndStatus(userId, RecipeHistoryStatus.ACTIVE, pageable);

            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent())
                .allMatch(h -> h.getRecipeCategoryId().equals(categoryId));
          }
        }
      }

      @Nested
      @DisplayName("Given - 카테고리가 없는 히스토리만 있을 때")
      class GivenOnlyUncategorizedHistories {

        private UUID userId;

        @BeforeEach
        void setUp() {
          userId = UUID.randomUUID();
          when(clock.now()).thenReturn(LocalDateTime.now());

          // 모든 히스토리가 카테고리 없음
          for (int i = 0; i < 3; i++) {
            RecipeHistory history = RecipeHistory.create(clock, userId, UUID.randomUUID());
            repository.save(history);
          }
        }

        @Nested
        @DisplayName("When - 사용자의 모든 레시피 히스토리를 조회한다면")
        class WhenFindingAllRecipeHistories {

          @Test
          @DisplayName("Then - 카테고리가 없는 모든 히스토리를 반환해야 한다")
          void thenShouldReturnAllUncategorizedHistories() {
            Pageable pageable = PageRequest.of(0, 10, RecipeHistorySort.VIEWED_AT_DESC);
            Page<RecipeHistory> result =
                repository.findAllByUserIdAndStatus(userId, RecipeHistoryStatus.ACTIVE, pageable);

            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent()).allMatch(h -> h.getRecipeCategoryId() == null);
          }
        }
      }

      @Nested
      @DisplayName("Given - 삭제된 히스토리가 포함되어 있을 때")
      class GivenDeletedHistoriesIncluded {

        private UUID userId;

        @BeforeEach
        void setUp() {
          userId = UUID.randomUUID();
          when(clock.now()).thenReturn(LocalDateTime.now());

          // Active 히스토리 2개
          for (int i = 0; i < 2; i++) {
            RecipeHistory history = RecipeHistory.create(clock, userId, UUID.randomUUID());
            repository.save(history);
          }

          // Deleted 히스토리 2개
          for (int i = 0; i < 2; i++) {
            RecipeHistory history = RecipeHistory.create(clock, userId, UUID.randomUUID());
            history.delete(clock);
            repository.save(history);
          }
        }

        @Nested
        @DisplayName("When - Active 상태의 히스토리를 조회한다면")
        class WhenFindingActiveHistories {

          @Test
          @DisplayName("Then - 삭제되지 않은 히스토리만 반환해야 한다")
          void thenShouldReturnOnlyActiveHistories() {
            Pageable pageable = PageRequest.of(0, 10, RecipeHistorySort.VIEWED_AT_DESC);
            Page<RecipeHistory> result =
                repository.findAllByUserIdAndStatus(userId, RecipeHistoryStatus.ACTIVE, pageable);

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                .allMatch(h -> h.getStatus() == RecipeHistoryStatus.ACTIVE);
          }
        }
      }

      @Nested
      @DisplayName("Given - 히스토리가 없는 사용자 ID가 주어졌을 때")
      class GivenUserIdWithNoHistories {

        private UUID userId;

        @BeforeEach
        void setUp() {
          userId = UUID.randomUUID();
        }

        @Nested
        @DisplayName("When - 사용자의 모든 레시피 히스토리를 조회한다면")
        class WhenFindingAllRecipeHistories {

          @Test
          @DisplayName("Then - 빈 페이지를 반환해야 한다")
          void thenShouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10, RecipeHistorySort.VIEWED_AT_DESC);
            Page<RecipeHistory> result =
                repository.findAllByUserIdAndStatus(userId, RecipeHistoryStatus.ACTIVE, pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
          }
        }
      }
    }

    @Nested
    @DisplayName("다른 사용자의 히스토리 분리")
    class SeparateHistoriesByUsers {

      @Nested
      @DisplayName("Given - 여러 사용자의 히스토리가 있을 때")
      class GivenMultipleUserHistories {

        private UUID userId1;
        private UUID userId2;

        @BeforeEach
        void setUp() {
          userId1 = UUID.randomUUID();
          userId2 = UUID.randomUUID();
          when(clock.now()).thenReturn(LocalDateTime.now());

          // userId1의 히스토리 3개
          for (int i = 0; i < 3; i++) {
            RecipeHistory history = RecipeHistory.create(clock, userId1, UUID.randomUUID());
            repository.save(history);
          }

          // userId2의 히스토리 2개
          for (int i = 0; i < 2; i++) {
            RecipeHistory history = RecipeHistory.create(clock, userId2, UUID.randomUUID());
            repository.save(history);
          }
        }

        @Nested
        @DisplayName("When - 특정 사용자의 모든 히스토리를 조회한다면")
        class WhenFindingSpecificUserHistories {

          @Test
          @DisplayName("Then - 해당 사용자의 히스토리만 반환해야 한다")
          void thenShouldReturnOnlySpecificUserHistories() {
            Pageable pageable = PageRequest.of(0, 10, RecipeHistorySort.VIEWED_AT_DESC);

            Page<RecipeHistory> user1Result =
                repository.findAllByUserIdAndStatus(userId1, RecipeHistoryStatus.ACTIVE, pageable);
            assertThat(user1Result.getContent()).hasSize(3);
            assertThat(user1Result.getContent()).allMatch(h -> h.getUserId().equals(userId1));

            Page<RecipeHistory> user2Result =
                repository.findAllByUserIdAndStatus(userId2, RecipeHistoryStatus.ACTIVE, pageable);
            assertThat(user2Result.getContent()).hasSize(2);
            assertThat(user2Result.getContent()).allMatch(h -> h.getUserId().equals(userId2));
          }
        }
      }
    }

    @Nested
    @DisplayName("카테고리별 레시피 조회 상태 개수 조회")
    class CountRecipeHistoryesByCategories {

      @Nested
      @DisplayName("Given - 여러 카테고리에 속한 레시피 조회 상태들이 존재할 때")
      class GivenRecipeHistoryesInMultipleCategories {

        private UUID userId;
        private UUID categoryId1;
        private UUID categoryId2;
        private UUID categoryId3;
        private List<UUID> categoryIds;

        @BeforeEach
        void setUp() {
          userId = UUID.randomUUID();
          categoryId1 = UUID.randomUUID();
          categoryId2 = UUID.randomUUID();
          categoryId3 = UUID.randomUUID();
          categoryIds = List.of(categoryId1, categoryId2, categoryId3);
          when(clock.now()).thenReturn(LocalDateTime.now());
        }

        @Nested
        @DisplayName("When - 카테고리별 레시피 조회 상태 개수를 조회한다면")
        class WhenCountingRecipeHistoryesByCategories {

          @BeforeEach
          void beforeEach() {
            IntStream.range(0, 3)
                .mapToObj(
                    i -> {
                      RecipeHistory status = RecipeHistory.create(clock, userId, UUID.randomUUID());
                      status.updateRecipeCategoryId(categoryId1);
                      return status;
                    })
                .forEach(repository::save);

            IntStream.range(0, 2)
                .mapToObj(
                    i -> {
                      RecipeHistory status = RecipeHistory.create(clock, userId, UUID.randomUUID());
                      status.updateRecipeCategoryId(categoryId2);
                      return status;
                    })
                .forEach(repository::save);

            IntStream.range(0, 3)
                .mapToObj(
                    i -> {
                      RecipeHistory status = RecipeHistory.create(clock, userId, UUID.randomUUID());
                      status.updateRecipeCategoryId(categoryId3);
                      status.delete(clock);
                      repository.save(status);
                      return status;
                    })
                .forEach(repository::save);
          }

          @Test
          @DisplayName("Then - 각 카테고리별 정확한 개수를 반환해야 한다")
          void thenShouldReturnCorrectCountsForEachCategory() {
            List<RecipeHistoryCategorizedCountProjection> counts =
                repository.countByCategoryIdsAndStatus(categoryIds, RecipeHistoryStatus.ACTIVE);

            assertThat(counts.size()).isEqualTo(2);

            RecipeHistoryCategorizedCountProjection count1 =
                counts.stream()
                    .filter(c -> c.getCategoryId().equals(categoryId1))
                    .findFirst()
                    .orElseThrow();
            assertThat(count1.getCount()).isEqualTo(3);

            RecipeHistoryCategorizedCountProjection count2 =
                counts.stream()
                    .filter(c -> c.getCategoryId().equals(categoryId2))
                    .findFirst()
                    .orElseThrow();
            assertThat(count2.getCount()).isEqualTo(2);

            boolean hasCategory3 =
                counts.stream().anyMatch(c -> c.getCategoryId().equals(categoryId3));
            assertThat(hasCategory3).isFalse();
          }
        }
      }

      @Nested
      @DisplayName("Given - 존재하지 않는 카테고리 ID들이 주어졌을 때")
      class GivenNonExistentRecipeCategoryIds {

        private List<UUID> nonExistentCategoryIds;

        @BeforeEach
        void setUp() {
          nonExistentCategoryIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        }

        @Nested
        @DisplayName("When - 카테고리별 레시피 조회 상태 개수를 조회한다면")
        class WhenCountingRecipeHistoryesByNonExistentCategories {

          @Test
          @DisplayName("Then - 빈 결과를 반환해야 한다")
          void thenShouldReturnEmpty() {
            List<RecipeHistoryCategorizedCountProjection> counts =
                repository.countByCategoryIdsAndStatus(
                    nonExistentCategoryIds, RecipeHistoryStatus.ACTIVE);
            assertThat(counts.size()).isEqualTo(0);
          }
        }
      }

      @Nested
      @DisplayName("Given - 빈 카테고리 ID 목록이 주어졌을 때")
      class GivenEmptyRecipeCategoryIds {

        @Nested
        @DisplayName("When - 카테고리별 레시피 조회 상태 개수를 조회한다면")
        class WhenCountingRecipeHistoryesByEmptyCategories {

          @Test
          @DisplayName("Then - 빈 결과를 반환해야 한다")
          void thenShouldReturnEmpty() {
            List<RecipeHistoryCategorizedCountProjection> counts =
                repository.countByCategoryIdsAndStatus(List.of(), RecipeHistoryStatus.ACTIVE);
            assertThat(counts.size()).isEqualTo(0);
          }
        }
      }
    }

    @Nested
    @DisplayName("사용자별 레시피 히스토리 개수 조회")
    class CountRecipeHistoriesByUserId {

      @Nested
      @DisplayName("Given - 레시피 히스토리가 있는 사용자 ID가 주어졌을 때")
      class GivenUserIdWithHistories {

        private UUID userId;

        @BeforeEach
        void setUp() {
          userId = UUID.randomUUID();
          when(clock.now()).thenReturn(LocalDateTime.now());
        }

        @Nested
        @DisplayName("When - 사용자의 레시피 히스토리 개수를 조회한다면")
        class WhenCountingHistoriesByUserId {

          @BeforeEach
          void beforeEach() {
            IntStream.range(0, 5)
                .forEach(
                    i -> {
                      RecipeHistory history =
                          RecipeHistory.create(clock, userId, UUID.randomUUID());
                      repository.save(history);
                    });

            IntStream.range(0, 3)
                .forEach(
                    i -> {
                      RecipeHistory history =
                          RecipeHistory.create(clock, userId, UUID.randomUUID());
                      history.delete(clock);
                      repository.save(history);
                    });
          }

          @Test
          @DisplayName("Then - Active 상태의 정확한 개수를 반환해야 한다")
          void thenShouldReturnCorrectActiveCount() {
            RecipeHistoryUnCategorizedCountProjection projection =
                repository.countByUserIdAndStatus(userId, RecipeHistoryStatus.ACTIVE);

            assertThat(projection.getCount()).isEqualTo(5);
          }
        }
      }

      @Nested
      @DisplayName("Given - 카테고리가 섞인 히스토리가 있을 때")
      class GivenHistoriesWithMixedCategories {

        private UUID userId;
        private UUID categoryId;

        @BeforeEach
        void setUp() {
          userId = UUID.randomUUID();
          categoryId = UUID.randomUUID();
          when(clock.now()).thenReturn(LocalDateTime.now());
        }

        @Nested
        @DisplayName("When - 사용자의 전체 히스토리 개수를 조회한다면")
        class WhenCountingAllHistories {

          @BeforeEach
          void beforeEach() {
            IntStream.range(0, 3)
                .forEach(
                    i -> {
                      RecipeHistory history =
                          RecipeHistory.create(clock, userId, UUID.randomUUID());
                      history.updateRecipeCategoryId(categoryId);
                      repository.save(history);
                    });

            IntStream.range(0, 2)
                .forEach(
                    i -> {
                      RecipeHistory history =
                          RecipeHistory.create(clock, userId, UUID.randomUUID());
                      repository.save(history);
                    });
          }

          @Test
          @DisplayName("Then - 카테고리 유무와 상관없이 모든 Active 히스토리 개수를 반환해야 한다")
          void thenShouldReturnAllActiveHistoriesCount() {
            RecipeHistoryUnCategorizedCountProjection projection =
                repository.countByUserIdAndStatus(userId, RecipeHistoryStatus.ACTIVE);

            assertThat(projection.getCount()).isEqualTo(5);
          }
        }
      }

      @Nested
      @DisplayName("Given - 히스토리가 없는 사용자 ID가 주어졌을 때")
      class GivenUserIdWithNoHistories {

        private UUID userId;

        @BeforeEach
        void setUp() {
          userId = UUID.randomUUID();
        }

        @Nested
        @DisplayName("When - 사용자의 레시피 히스토리 개수를 조회한다면")
        class WhenCountingHistoriesByUserId {

          @Test
          @DisplayName("Then - 0을 반환해야 한다")
          void thenShouldReturnZero() {
            RecipeHistoryUnCategorizedCountProjection projection =
                repository.countByUserIdAndStatus(userId, RecipeHistoryStatus.ACTIVE);

            assertThat(projection.getCount()).isEqualTo(0);
          }
        }
      }

      @Nested
      @DisplayName("Given - 여러 사용자의 히스토리가 섞여있을 때")
      class GivenMultipleUsersHistories {

        private UUID userId1;
        private UUID userId2;

        @BeforeEach
        void setUp() {
          userId1 = UUID.randomUUID();
          userId2 = UUID.randomUUID();
          when(clock.now()).thenReturn(LocalDateTime.now());
        }

        @Nested
        @DisplayName("When - 특정 사용자의 히스토리 개수를 조회한다면")
        class WhenCountingSpecificUserHistories {

          @BeforeEach
          void beforeEach() {
            IntStream.range(0, 3)
                .forEach(
                    i -> {
                      RecipeHistory history =
                          RecipeHistory.create(clock, userId1, UUID.randomUUID());
                      repository.save(history);
                    });

            IntStream.range(0, 5)
                .forEach(
                    i -> {
                      RecipeHistory history =
                          RecipeHistory.create(clock, userId2, UUID.randomUUID());
                      repository.save(history);
                    });
          }

          @Test
          @DisplayName("Then - 해당 사용자의 히스토리만 카운트되어야 한다")
          void thenShouldCountOnlySpecificUserHistories() {
            RecipeHistoryUnCategorizedCountProjection projection1 =
                repository.countByUserIdAndStatus(userId1, RecipeHistoryStatus.ACTIVE);
            assertThat(projection1.getCount()).isEqualTo(3);

            RecipeHistoryUnCategorizedCountProjection projection2 =
                repository.countByUserIdAndStatus(userId2, RecipeHistoryStatus.ACTIVE);
            assertThat(projection2.getCount()).isEqualTo(5);
          }
        }
      }
    }

    @Nested
    @DisplayName("사용자의 레시피 조회 상태 목록 조회")
    class FindRecipeHistoryesByRecipeIdsAndUserId {

      @Nested
      @DisplayName("Given - 유효한 레시피 ID 목록과 사용자 ID가 주어졌을 때")
      class GivenValidRecipeIdsAndUserId {

        private List<UUID> recipeIds;
        private UUID userId;

        @BeforeEach
        void setUp() {
          userId = UUID.randomUUID();
          recipeIds = new ArrayList<>();
          when(clock.now()).thenReturn(LocalDateTime.now());

          for (int i = 0; i < 3; i++) {
            UUID recipeId = UUID.randomUUID();
            recipeIds.add(recipeId);
            RecipeHistory viewStatus = RecipeHistory.create(clock, userId, recipeId);
            repository.save(viewStatus);
          }
        }

        @Nested
        @DisplayName("When - 사용자의 레시피 조회 상태 목록을 조회한다면")
        class WhenFindingRecipeHistoryesByRecipeIdsAndUserId {

          @Test
          @DisplayName("Then - 해당 레시피 조회 상태 목록을 반환해야 한다")
          void thenShouldReturnRecipeHistoryList() {
            List<RecipeHistory> statuses =
                repository.findByRecipeIdInAndUserIdAndStatus(
                    recipeIds, userId, RecipeHistoryStatus.ACTIVE);

            assertThat(statuses).hasSize(3);
            assertThat(statuses)
                .allMatch(status -> status.getUserId().equals(userId))
                .allMatch(status -> recipeIds.contains(status.getRecipeId()));
          }
        }
      }

      @Nested
      @DisplayName("Given - 일부만 조회한 레시피 ID 목록이 주어졌을 때")
      class GivenPartiallyViewedRecipeIds {

        private List<UUID> allRecipeIds;
        private List<UUID> viewedRecipeIds;
        private UUID userId;

        @BeforeEach
        void setUp() {
          userId = UUID.randomUUID();
          allRecipeIds = new ArrayList<>();
          viewedRecipeIds = new ArrayList<>();
          when(clock.now()).thenReturn(LocalDateTime.now());

          // 5개의 레시피 ID 중 3개만 조회 상태 생성
          for (int i = 0; i < 5; i++) {
            UUID recipeId = UUID.randomUUID();
            allRecipeIds.add(recipeId);

            if (i < 3) {
              viewedRecipeIds.add(recipeId);
              RecipeHistory viewStatus = RecipeHistory.create(clock, userId, recipeId);
              repository.save(viewStatus);
            }
          }
        }

        @Nested
        @DisplayName("When - 모든 레시피 ID로 조회한다면")
        class WhenFindingWithAllRecipeIds {

          @Test
          @DisplayName("Then - 조회한 레시피만 반환해야 한다")
          void thenShouldReturnOnlyViewedRecipes() {
            List<RecipeHistory> statuses =
                repository.findByRecipeIdInAndUserIdAndStatus(
                    allRecipeIds, userId, RecipeHistoryStatus.ACTIVE);

            assertThat(statuses).hasSize(3);
            assertThat(statuses).allMatch(status -> viewedRecipeIds.contains(status.getRecipeId()));
          }
        }
      }

      @Nested
      @DisplayName("Given - 다른 사용자가 조회한 레시피가 있을 때")
      class GivenOtherUserViewedRecipes {

        private List<UUID> recipeIds;
        private UUID userId1;
        private UUID userId2;

        @BeforeEach
        void setUp() {
          userId1 = UUID.randomUUID();
          userId2 = UUID.randomUUID();
          recipeIds = new ArrayList<>();
          when(clock.now()).thenReturn(LocalDateTime.now());

          // userId1이 3개 조회
          for (int i = 0; i < 3; i++) {
            UUID recipeId = UUID.randomUUID();
            recipeIds.add(recipeId);
            RecipeHistory viewStatus = RecipeHistory.create(clock, userId1, recipeId);
            repository.save(viewStatus);
          }

          // userId2도 같은 레시피 조회
          for (UUID recipeId : recipeIds) {
            RecipeHistory viewStatus = RecipeHistory.create(clock, userId2, recipeId);
            repository.save(viewStatus);
          }
        }

        @Nested
        @DisplayName("When - 특정 사용자의 레시피 조회 상태를 조회한다면")
        class WhenFindingSpecificUserRecipeHistoryes {

          @Test
          @DisplayName("Then - 해당 사용자의 조회 상태만 반환해야 한다")
          void thenShouldReturnOnlySpecificUserStatuses() {
            List<RecipeHistory> user1Statuses =
                repository.findByRecipeIdInAndUserIdAndStatus(
                    recipeIds, userId1, RecipeHistoryStatus.ACTIVE);

            assertThat(user1Statuses).hasSize(3);
            assertThat(user1Statuses).allMatch(status -> status.getUserId().equals(userId1));

            List<RecipeHistory> user2Statuses =
                repository.findByRecipeIdInAndUserIdAndStatus(
                    recipeIds, userId2, RecipeHistoryStatus.ACTIVE);

            assertThat(user2Statuses).hasSize(3);
            assertThat(user2Statuses).allMatch(status -> status.getUserId().equals(userId2));
          }
        }
      }

      @Nested
      @DisplayName("Given - 삭제된 조회 상태가 포함된 경우")
      class GivenDeletedViewStatuses {

        private List<UUID> recipeIds;
        private UUID userId;

        @BeforeEach
        void setUp() {
          userId = UUID.randomUUID();
          recipeIds = new ArrayList<>();
          when(clock.now()).thenReturn(LocalDateTime.now());

          // 3개 생성, 1개는 삭제
          for (int i = 0; i < 3; i++) {
            UUID recipeId = UUID.randomUUID();
            recipeIds.add(recipeId);
            RecipeHistory viewStatus = RecipeHistory.create(clock, userId, recipeId);

            if (i == 1) {
              viewStatus.delete(clock);
            }
            repository.save(viewStatus);
          }
        }

        @Nested
        @DisplayName("When - Active 상태의 조회 상태를 조회한다면")
        class WhenFindingActiveStatuses {

          @Test
          @DisplayName("Then - 삭제되지 않은 조회 상태만 반환해야 한다")
          void thenShouldReturnOnlyActiveStatuses() {
            List<RecipeHistory> statuses =
                repository.findByRecipeIdInAndUserIdAndStatus(
                    recipeIds, userId, RecipeHistoryStatus.ACTIVE);

            assertThat(statuses).hasSize(2);
            assertThat(statuses)
                .allMatch(status -> status.getStatus() == RecipeHistoryStatus.ACTIVE);
          }
        }
      }

      @Nested
      @DisplayName("Given - 빈 레시피 ID 목록이 주어졌을 때")
      class GivenEmptyRecipeIds {

        private UUID userId;

        @BeforeEach
        void setUp() {
          userId = UUID.randomUUID();
        }

        @Nested
        @DisplayName("When - 레시피 조회 상태를 조회한다면")
        class WhenFindingRecipeHistoryes {

          @Test
          @DisplayName("Then - 빈 목록을 반환해야 한다")
          void thenShouldReturnEmptyList() {
            List<RecipeHistory> statuses =
                repository.findByRecipeIdInAndUserIdAndStatus(
                    List.of(), userId, RecipeHistoryStatus.ACTIVE);

            assertThat(statuses).isEmpty();
          }
        }
      }

      @Nested
      @DisplayName("Given - 존재하지 않는 레시피 ID 목록이 주어졌을 때")
      class GivenNonExistentRecipeIds {

        private List<UUID> nonExistentRecipeIds;
        private UUID userId;

        @BeforeEach
        void setUp() {
          userId = UUID.randomUUID();
          nonExistentRecipeIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        }

        @Nested
        @DisplayName("When - 레시피 조회 상태를 조회한다면")
        class WhenFindingRecipeHistoryes {

          @Test
          @DisplayName("Then - 빈 목록을 반환해야 한다")
          void thenShouldReturnEmptyList() {
            List<RecipeHistory> statuses =
                repository.findByRecipeIdInAndUserIdAndStatus(
                    nonExistentRecipeIds, userId, RecipeHistoryStatus.ACTIVE);

            assertThat(statuses).isEmpty();
          }
        }
      }
    }

    @Nested
    @DisplayName("레시피 조회 상태 삭제")
    class DeleteRecipeHistory {

      @Nested
      @DisplayName("Given - 유효한 레시피 ID와 사용자 ID가 주어졌을 때")
      class GivenValidRecipeIdAndUserId {

        private UUID recipeId;
        private UUID userId;

        @BeforeEach
        void setUp() {
          recipeId = UUID.randomUUID();
          userId = UUID.randomUUID();
          when(clock.now()).thenReturn(LocalDateTime.now());
        }

        @Nested
        @DisplayName("When - 레시피 조회 상태를 삭제한다면")
        class WhenDeletingRecipeHistory {

          private RecipeHistory recipeHistory;

          @BeforeEach
          void beforeEach() {
            recipeHistory = RecipeHistory.create(clock, userId, recipeId);
            repository.save(recipeHistory);
            repository.delete(recipeHistory);
          }

          @Test
          @DisplayName("Then - 해당 레시피 조회 상태가 삭제되어야 한다")
          void thenShouldDeleteRecipeHistory() {
            assertThat(
                    repository.findByRecipeIdAndUserIdAndStatus(
                        recipeId, userId, RecipeHistoryStatus.ACTIVE))
                .isEmpty();
          }
        }
      }
    }
  }
}
