package com.cheftory.api.recipeinfo.viewstatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
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

@DisplayName("RecipeViewStatusRepository Tests")
public class RecipeViewStatusRepositoryTest extends DbContextTest {

  @Autowired private RecipeViewStatusRepository repository;
  @MockitoBean private Clock clock;

  @BeforeEach
  void setUp() {
    // Mock을 초기화하지 말고 그대로 사용
    // clock = new Clock(); // 이 줄 제거!
  }

  @Nested
  @DisplayName("레시피 조회 상태 저장")
  class SaveRecipeViewStatus {

    @Nested
    @DisplayName("Given - 유효한 레시피 조회 상태가 주어졌을 때")
    class GivenValidRecipeViewStatus {

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
      class WhenSavingRecipeViewStatus {

        private RecipeViewStatus recipeViewStatus;

        @BeforeEach
        void beforeEach() {
          recipeViewStatus = RecipeViewStatus.create(clock, userId, recipeId);
          repository.save(recipeViewStatus);
        }

        @DisplayName("Then - 레시피 조회 상태가 저장되어야 한다")
        @Test
        void thenShouldSaveRecipeViewStatus() {
          RecipeViewStatus savedStatus =
              repository.findById(recipeViewStatus.getId()).orElseThrow();
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

        private RecipeViewStatus recipeViewStatus;

        @BeforeEach
        void beforeEach() {
          recipeViewStatus = RecipeViewStatus.create(clock, userId, recipeId);
          repository.save(recipeViewStatus);
        }

        @Test
        @DisplayName("Then - 해당 레시피 조회 상태가 존재해야 한다")
        void thenShouldExist() {
          boolean exists =
              repository.existsByRecipeIdAndUserIdAndStatus(
                  recipeId, userId, RecipeViewState.ACTIVE);
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
                  recipeId, userId, RecipeViewState.ACTIVE);
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

        private RecipeViewStatus recipeViewStatus;

        @BeforeEach
        void beforeEach() {
          recipeViewStatus = RecipeViewStatus.create(clock, userId, recipeId);
          repository.save(recipeViewStatus);
        }

        @Test
        @DisplayName("Then - 해당 레시피 조회 상태를 반환해야 한다")
        void thenShouldReturnRecipeViewStatus() {
          RecipeViewStatus foundStatus =
              repository
                  .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeViewState.ACTIVE)
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
                      recipeId, userId, RecipeViewState.ACTIVE))
              .isEmpty();
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 조회 상태 미분류 카테고리 조회")
  class FindUncategorizedRecipeViewStatuses {

    @Nested
    @DisplayName("Given - 미분류 레시피 조회 상태가 존재할 때")
    class GivenUncategorizedRecipeViewStatuses {

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

        private RecipeViewStatus recipeViewStatus;

        @BeforeEach
        void beforeEach() {
          recipeViewStatus = RecipeViewStatus.create(clock, userId, recipeId);
          repository.save(recipeViewStatus);
        }

        @Test
        @DisplayName("Then - 해당 레시피 조회 상태를 반환해야 한다")
        void thenShouldReturnUncategorizedRecipeViewStatuses() {

          Pageable pageable = PageRequest.of(page, 10);
          var statuses =
              repository
                  .findAllByUserIdAndRecipeCategoryIdAndStatus(
                      userId, null, RecipeViewState.ACTIVE, pageable)
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
    class GivenNoUncategorizedRecipeViewStatuses {

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
                      userId, null, RecipeViewState.ACTIVE, pageable)
                  .getContent();
          assertThat(statuses.size()).isEqualTo(0);
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 조회 상태 분류 카테고리 조회")
  class FindCategorizedRecipeViewStatuses {

    @Nested
    @DisplayName("Given - 분류된 레시피 조회 상태가 존재할 때")
    class GivenCategorizedRecipeViewStatuses {

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

        private RecipeViewStatus recipeViewStatus;

        @BeforeEach
        void beforeEach() {
          recipeViewStatus = RecipeViewStatus.create(clock, userId, recipeId);
          recipeViewStatus.updateRecipeCategoryId(categoryId);
          repository.save(recipeViewStatus);
        }

        @Test
        @DisplayName("Then - 해당 레시피 조회 상태를 반환해야 한다")
        void thenShouldReturnCategorizedRecipeViewStatuses() {

          Pageable pageable = PageRequest.of(page, 10);
          var statuses =
              repository
                  .findAllByUserIdAndRecipeCategoryIdAndStatus(
                      userId, categoryId, RecipeViewState.ACTIVE, pageable)
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
    class GivenNoCategorizedRecipeViewStatuses {

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
                      userId, categoryId, RecipeViewState.ACTIVE, pageable)
                  .getContent();
          assertThat(statuses.size()).isEqualTo(0);
        }
      }
    }
  }

  @Nested
  @DisplayName("사용자의 최근 조회 레시피 상태 조회")
  class FindRecentRecipeViewStatusesByUserId {

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
      class WhenFindingRecentRecipeViewStatusesByUserId {

        private UUID firstRecipeId;
        private UUID secondRecipeId;
        private RecipeViewStatus firstRecipeViewStatus;
        private RecipeViewStatus secondRecipeViewStatus;

        @BeforeEach
        void beforeEach() {
          firstRecipeId = UUID.randomUUID();
          secondRecipeId = UUID.randomUUID();

          // 첫 번째는 더 이전 시간으로 설정
          LocalDateTime firstTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
          when(clock.now()).thenReturn(firstTime);
          firstRecipeViewStatus = RecipeViewStatus.create(clock, userId, firstRecipeId);
          repository.save(firstRecipeViewStatus);

          // 두 번째는 더 최근 시간으로 설정
          LocalDateTime secondTime = LocalDateTime.of(2024, 1, 1, 11, 0, 0);
          when(clock.now()).thenReturn(secondTime);
          secondRecipeViewStatus = RecipeViewStatus.create(clock, userId, secondRecipeId);
          repository.save(secondRecipeViewStatus);
        }

        @Test
        @DisplayName("Then - 최근 조회 순서로 정렬된 레시피 상태들을 반환해야 한다")
        void thenShouldReturnRecentRecipeViewStatusesByUserId() {
          Pageable pageable = PageRequest.of(0, 10, ViewStatusSort.VIEWED_AT_DESC);
          Page<RecipeViewStatus> page =
              repository.findByUserIdAndStatus(userId, RecipeViewState.ACTIVE, pageable);
          List<RecipeViewStatus> statuses = page.getContent();

          assertThat(statuses).hasSize(2);
          assertThat(statuses.get(0).getUserId()).isEqualTo(userId);

          // 더 최근 것(11시)이 먼저 나와야 함
          assertThat(statuses.get(0).getRecipeId()).isEqualTo(secondRecipeViewStatus.getRecipeId());
          assertThat(statuses.get(1).getRecipeId()).isEqualTo(firstRecipeViewStatus.getRecipeId());

          // 시간 검증 - 첫 번째가 더 최근이어야 함
          assertThat(statuses.get(0).getViewedAt()).isAfter(statuses.get(1).getViewedAt());
        }
      }

      @Nested
      @DisplayName("When - 사용자의 10개의 최근 조회 레시피를 가져온다면")
      class WhenFindinTengRecentRecipeViewStatusesByUserId {

        private List<UUID> recipeIds;
        private List<RecipeViewStatus> recipeViewStatuses;
        private LocalDateTime baseTime;

        @BeforeEach
        void beforeEach() {
          recipeIds = new ArrayList<>();
          recipeViewStatuses = new ArrayList<>();
          baseTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);

          // 20개의 레시피 조회 상태 생성 (시간 순서대로)
          for (int i = 0; i < 20; i++) {
            UUID recipeId = UUID.randomUUID();
            recipeIds.add(recipeId);

            // 각각 1분씩 차이나게 설정 (i=0이 가장 오래된 것)
            LocalDateTime viewTime = baseTime.plusMinutes(i);
            when(clock.now()).thenReturn(viewTime);

            RecipeViewStatus viewStatus = RecipeViewStatus.create(clock, userId, recipeId);
            recipeViewStatuses.add(viewStatus);
            repository.save(viewStatus);
          }
        }

        @Test
        @DisplayName("Then - 최근 조회 순서로 정렬된 레시피 상태들을 반환해야 한다")
        void thenShouldReturnRecentRecipeViewStatusesByUserId() {
          // Given
          Pageable pageable = PageRequest.of(0, 10, ViewStatusSort.VIEWED_AT_DESC);

          // When
          Page<RecipeViewStatus> statusPage =
              repository.findByUserIdAndStatus(userId, RecipeViewState.ACTIVE, pageable);

          // Then
          assertThat(statusPage.getTotalElements()).isEqualTo(20);
          assertThat(statusPage.getContent()).hasSize(10); // 페이지 크기만큼만
          assertThat(statusPage.getNumber()).isEqualTo(0); // 현재 페이지
          assertThat(statusPage.getSize()).isEqualTo(10); // 페이지 크기
          assertThat(statusPage.getTotalPages()).isEqualTo(2); // 총 페이지 수

          List<RecipeViewStatus> statuses = statusPage.getContent();
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
          Pageable firstPageable = PageRequest.of(0, 10, ViewStatusSort.VIEWED_AT_DESC);

          // When
          Page<RecipeViewStatus> firstPage =
              repository.findByUserIdAndStatus(userId, RecipeViewState.ACTIVE, firstPageable);

          // Then
          assertThat(firstPage.getTotalElements()).isEqualTo(20);
          assertThat(firstPage.getContent()).hasSize(10);
          assertThat(firstPage.getNumber()).isEqualTo(0);
          assertThat(firstPage.getTotalPages()).isEqualTo(2);
          assertThat(firstPage.hasNext()).isTrue();
          assertThat(firstPage.hasPrevious()).isFalse();

          // 가장 최근 10개가 첫 페이지에 나와야 함 (인덱스 19~10)
          List<RecipeViewStatus> firstPageContent = firstPage.getContent();
          assertThat(firstPageContent.get(0).getRecipeId()).isEqualTo(recipeIds.get(19)); // 가장 최근
          assertThat(firstPageContent.get(9).getRecipeId())
              .isEqualTo(recipeIds.get(10)); // 10번째로 최근

          // Given - 두 번째 페이지
          Pageable secondPageable = PageRequest.of(1, 10, ViewStatusSort.VIEWED_AT_DESC);

          // When
          Page<RecipeViewStatus> secondPage =
              repository.findByUserIdAndStatus(userId, RecipeViewState.ACTIVE, secondPageable);

          // Then
          assertThat(secondPage.getTotalElements()).isEqualTo(20);
          assertThat(secondPage.getContent()).hasSize(10);
          assertThat(secondPage.getNumber()).isEqualTo(1);
          assertThat(secondPage.getTotalPages()).isEqualTo(2);
          assertThat(secondPage.hasNext()).isFalse();
          assertThat(secondPage.hasPrevious()).isTrue();

          // 나머지 10개가 두 번째 페이지에 나와야 함 (인덱스 9~0)
          List<RecipeViewStatus> secondPageContent = secondPage.getContent();
          assertThat(secondPageContent.get(0).getRecipeId())
              .isEqualTo(recipeIds.get(9)); // 11번째로 최근
          assertThat(secondPageContent.get(9).getRecipeId()).isEqualTo(recipeIds.get(0)); // 가장 오래된
        }

        @Test
        @DisplayName("Then - 빈 결과에 대해서도 올바른 페이지 정보를 반환해야 한다")
        void thenShouldReturnCorrectPageInfoForEmptyResult() {
          // Given
          UUID nonExistentUserId = UUID.randomUUID();
          Pageable pageable = PageRequest.of(0, 10, ViewStatusSort.VIEWED_AT_DESC);

          // When
          Page<RecipeViewStatus> emptyPage =
              repository.findByUserIdAndStatus(nonExistentUserId, RecipeViewState.ACTIVE, pageable);

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
          Pageable pageable = PageRequest.of(0, 5, ViewStatusSort.VIEWED_AT_DESC);

          // When
          Page<RecipeViewStatus> page =
              repository.findByUserIdAndStatus(userId, RecipeViewState.ACTIVE, pageable);

          // Then
          assertThat(page.getTotalElements()).isEqualTo(20);
          assertThat(page.getContent()).hasSize(5);
          assertThat(page.getTotalPages()).isEqualTo(4); // 20 / 5 = 4페이지
          assertThat(page.hasNext()).isTrue();

          // 가장 최근 5개가 나와야 함 (인덱스 19~15)
          List<RecipeViewStatus> content = page.getContent();
          assertThat(content.get(0).getRecipeId()).isEqualTo(recipeIds.get(19));
          assertThat(content.get(4).getRecipeId()).isEqualTo(recipeIds.get(15));
        }
      }
    }

    @Nested
    @DisplayName("카테고리별 레시피 조회 상태 개수 조회")
    class CountRecipeViewStatusesByCategories {

      @Nested
      @DisplayName("Given - 여러 카테고리에 속한 레시피 조회 상태들이 존재할 때")
      class GivenRecipeViewStatusesInMultipleCategories {

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
        class WhenCountingRecipeViewStatusesByCategories {

          @BeforeEach
          void beforeEach() {
            IntStream.range(0, 3)
                .mapToObj(
                    i -> {
                      RecipeViewStatus status =
                          RecipeViewStatus.create(clock, userId, UUID.randomUUID());
                      status.updateRecipeCategoryId(categoryId1);
                      return status;
                    })
                .forEach(repository::save);

            IntStream.range(0, 2)
                .mapToObj(
                    i -> {
                      RecipeViewStatus status =
                          RecipeViewStatus.create(clock, userId, UUID.randomUUID());
                      status.updateRecipeCategoryId(categoryId2);
                      return status;
                    })
                .forEach(repository::save);

            IntStream.range(0, 3)
                .mapToObj(
                    i -> {
                      RecipeViewStatus status =
                          RecipeViewStatus.create(clock, userId, UUID.randomUUID());
                      status.updateRecipeCategoryId(categoryId3);
                      status.delete();
                      repository.save(status);
                      return status;
                    })
                .forEach(repository::save);
          }

          @Test
          @DisplayName("Then - 각 카테고리별 정확한 개수를 반환해야 한다")
          void thenShouldReturnCorrectCountsForEachCategory() {
            List<RecipeViewStatusCountProjection> counts =
                repository.countByCategoryIdsAndStatus(categoryIds, RecipeViewState.ACTIVE);

            assertThat(counts.size()).isEqualTo(2);

            RecipeViewStatusCountProjection count1 =
                counts.stream()
                    .filter(c -> c.getCategoryId().equals(categoryId1))
                    .findFirst()
                    .orElseThrow();
            assertThat(count1.getCount()).isEqualTo(3);

            RecipeViewStatusCountProjection count2 =
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
      class GivenNonExistentCategoryIds {

        private List<UUID> nonExistentCategoryIds;

        @BeforeEach
        void setUp() {
          nonExistentCategoryIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        }

        @Nested
        @DisplayName("When - 카테고리별 레시피 조회 상태 개수를 조회한다면")
        class WhenCountingRecipeViewStatusesByNonExistentCategories {

          @Test
          @DisplayName("Then - 빈 결과를 반환해야 한다")
          void thenShouldReturnEmpty() {
            List<RecipeViewStatusCountProjection> counts =
                repository.countByCategoryIdsAndStatus(
                    nonExistentCategoryIds, RecipeViewState.ACTIVE);
            assertThat(counts.size()).isEqualTo(0);
          }
        }
      }

      @Nested
      @DisplayName("Given - 빈 카테고리 ID 목록이 주어졌을 때")
      class GivenEmptyCategoryIds {

        @Nested
        @DisplayName("When - 카테고리별 레시피 조회 상태 개수를 조회한다면")
        class WhenCountingRecipeViewStatusesByEmptyCategories {

          @Test
          @DisplayName("Then - 빈 결과를 반환해야 한다")
          void thenShouldReturnEmpty() {
            List<RecipeViewStatusCountProjection> counts =
                repository.countByCategoryIdsAndStatus(List.of(), RecipeViewState.ACTIVE);
            assertThat(counts.size()).isEqualTo(0);
          }
        }
      }
    }

    @Nested
    @DisplayName("레시피 조회 상태 삭제")
    class DeleteRecipeViewStatus {

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
        class WhenDeletingRecipeViewStatus {

          private RecipeViewStatus recipeViewStatus;

          @BeforeEach
          void beforeEach() {
            recipeViewStatus = RecipeViewStatus.create(clock, userId, recipeId);
            repository.save(recipeViewStatus);
            repository.delete(recipeViewStatus);
          }

          @Test
          @DisplayName("Then - 해당 레시피 조회 상태가 삭제되어야 한다")
          void thenShouldDeleteRecipeViewStatus() {
            assertThat(
                    repository.findByRecipeIdAndUserIdAndStatus(
                        recipeId, userId, RecipeViewState.ACTIVE))
                .isEmpty();
          }
        }
      }
    }
  }
}
