package com.cheftory.api.recipeinfo.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.history.exception.RecipeHistoryErrorCode;
import com.cheftory.api.recipeinfo.history.exception.RecipeHistoryException;
import com.cheftory.api.recipeinfo.util.RecipePageRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DisplayName("RecipeHistoryService Tests")
public class RecipeHistoryServiceTest {

  private RecipeHistoryRepository repository;
  private RecipeHistoryService service;
  private Clock clock;

  @BeforeEach
  void setUp() {
    repository = mock(RecipeHistoryRepository.class);
    clock = mock(Clock.class);
    service = new RecipeHistoryService(repository, clock);
  }

  @Nested
  @DisplayName("레시피 조회 상태 생성")
  class CreateRecipeHistory {

    @Nested
    @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
    class GivenValidParameters {

      private UUID recipeId;
      private UUID userId;
      private LocalDateTime fixedTime;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        userId = UUID.randomUUID();
        fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        doReturn(fixedTime).when(clock).now();
      }

      @Nested
      @DisplayName("When - 레시피 조회 상태를 생성한다면")
      class WhenCreatingRecipeHistory {

        @BeforeEach
        void beforeEach() {
          doReturn(false)
              .when(repository)
              .existsByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeHistoryStatus.ACTIVE);
        }

        @Test
        @DisplayName("Then - 올바른 파라미터로 Repository의 save가 호출되어야 한다")
        public void thenShouldCallRepositorySaveWithCorrectParameters() {
          service.create(userId, recipeId);

          verify(repository)
              .existsByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeHistoryStatus.ACTIVE);
          verify(repository)
              .save(
                  argThat(
                      RecipeHistory ->
                          RecipeHistory.getRecipeId().equals(recipeId)
                              && RecipeHistory.getUserId().equals(userId)));
        }
      }
    }

    @Nested
    @DisplayName("Given - 이미 조회한 레시피 상태가 있을 때")
    class GivenExistingRecipeHistory {

      private UUID recipeId;
      private UUID userId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        userId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 레시피 조회 상태를 생성한다면")
      class WhenCreatingExistingRecipeHistory {

        @BeforeEach
        void beforeEach() {
          doReturn(true)
              .when(repository)
              .existsByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeHistoryStatus.ACTIVE);
        }

        @Test
        @DisplayName("Then - 아무일도 일어나지 않아야 한다")
        public void thenShouldNotDoAnything() {
          service.create(userId, recipeId);

          verify(repository, never()).save(any(RecipeHistory.class));
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 조회 상태 조회")
  class FindRecipeHistory {

    @Nested
    @DisplayName("Given - 유효한 레시피 ID와 사용자 ID가 주어졌을 때")
    class GivenValidRecipeAndUserId {

      private UUID recipeId;
      private UUID userId;
      private LocalDateTime initialTime;
      private LocalDateTime updateTime;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        userId = UUID.randomUUID();
        initialTime = LocalDateTime.of(2023, 12, 31, 12, 0, 0);
        updateTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
      }

      @Nested
      @DisplayName("When - 레시피 조회 상태를 조회한다면")
      class WhenFindingRecipeHistory {

        private RecipeHistory realRecipeHistory;

        @BeforeEach
        void beforeEach() {
          doReturn(initialTime).when(clock).now();
          realRecipeHistory = RecipeHistory.create(clock, userId, recipeId);

          doReturn(updateTime).when(clock).now();

          doReturn(Optional.of(realRecipeHistory))
              .when(repository)
              .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeHistoryStatus.ACTIVE);
          doReturn(realRecipeHistory).when(repository).save(any(RecipeHistory.class));
        }

        @Test
        @DisplayName("Then - 올바른 레시피 조회 상태가 반환되어야 한다")
        public void thenShouldReturnCorrectRecipeHistory() {
          RecipeHistory status = service.get(userId, recipeId);

          assertThat(status).isNotNull();
          assertThat(status.getRecipeId()).isEqualTo(recipeId);
          assertThat(status.getUserId()).isEqualTo(userId);
          assertThat(status.getViewedAt()).isEqualTo(updateTime); // 실제로 업데이트된 시간

          verify(repository)
              .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeHistoryStatus.ACTIVE);
          verify(repository).save(realRecipeHistory);
        }
      }
    }

    @Nested
    @DisplayName("Given - 존재하지 않는 레시피 ID와 사용자 ID가 주어졌을 때")
    class GivenNonExistentRecipeAndUserId {

      private UUID recipeId;
      private UUID userId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        userId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 레시피 조회 상태를 조회한다면")
      class WhenFindingNonExistentRecipeHistory {

        @BeforeEach
        void beforeEach() {
          doReturn(Optional.empty())
              .when(repository)
              .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeHistoryStatus.ACTIVE);
        }

        @Test
        @DisplayName("Then - ViewStatusException이 발생해야 한다")
        public void thenShouldThrowViewStatusException() {
          RecipeHistoryException exception =
              assertThrows(
                  RecipeHistoryException.class,
                  () -> {
                    service.get(userId, recipeId);
                  });

          assertThat(exception.getErrorMessage())
              .isEqualTo(RecipeHistoryErrorCode.RECIPE_HISTORY_NOT_FOUND);
          verify(repository, never()).save(any());
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 조회 상태 카테고리 변경")
  class ChangeRecipeHistoryCategory {

    @Nested
    @DisplayName("Given - 유효한 레시피 ID와 사용자 ID가 주어졌을 때")
    class GivenValidRecipeAndUserId {

      private UUID recipeId;
      private UUID userId;
      private UUID newCategoryId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        userId = UUID.randomUUID();
        newCategoryId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 레시피 조회 상태의 카테고리를 변경한다면")
      class WhenChangingRecipeHistoryCategory {

        private RecipeHistory realRecipeHistory;

        @BeforeEach
        void beforeEach() {
          realRecipeHistory = RecipeHistory.create(clock, userId, recipeId);
          doReturn(Optional.of(realRecipeHistory))
              .when(repository)
              .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeHistoryStatus.ACTIVE);
          doReturn(realRecipeHistory).when(repository).save(any(RecipeHistory.class));
        }

        @Test
        @DisplayName("Then - 카테고리가 올바르게 변경되어야 한다")
        public void thenShouldChangeCategoryCorrectly() {
          service.updateCategory(userId, recipeId, newCategoryId);

          assertThat(realRecipeHistory.getRecipeCategoryId()).isEqualTo(newCategoryId);
          verify(repository)
              .save(
                  argThat(
                      RecipeHistory ->
                          RecipeHistory.getRecipeId().equals(recipeId)
                              && RecipeHistory.getUserId().equals(userId)
                              && RecipeHistory.getRecipeCategoryId().equals(newCategoryId)));
        }
      }
    }

    @Nested
    @DisplayName("Given - 존재하지 않는 레시피 ID와 사용자 ID가 주어졌을 때")
    class GivenNonExistentRecipeAndUserId {

      private UUID recipeId;
      private UUID userId;
      private UUID newCategoryId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        userId = UUID.randomUUID();
        newCategoryId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 레시피 조회 상태의 카테고리를 변경한다면")
      class WhenChangingNonExistentRecipeHistoryCategory {

        @BeforeEach
        void beforeEach() {
          doReturn(Optional.empty())
              .when(repository)
              .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeHistoryStatus.ACTIVE);
        }

        @Test
        @DisplayName("Then - ViewStatusException이 발생해야 한다")
        public void thenShouldThrowViewStatusException() {
          RecipeHistoryException exception =
              assertThrows(
                  RecipeHistoryException.class,
                  () -> {
                    service.updateCategory(userId, recipeId, newCategoryId);
                  });

          assertThat(exception.getErrorMessage())
              .isEqualTo(RecipeHistoryErrorCode.RECIPE_HISTORY_NOT_FOUND);
          verify(repository, never()).save(any());
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 조회 상태 카테고리 삭제")
  class DeleteRecipeHistoryCategory {

    @Nested
    @DisplayName("Given - 삭제할 카테고리 ID가 주어졌을 때")
    class GivenCategoryIdToDelete {

      private UUID categoryId;
      private LocalDateTime fixedTime;

      @BeforeEach
      void setUp() {
        categoryId = UUID.randomUUID();
        fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        doReturn(fixedTime).when(clock).now();
      }

      @Nested
      @DisplayName("When - 해당 카테고리를 가진 레시피 조회 상태들이 존재한다면")
      class WhenRecipeHistoryesWithCategoryExist {

        private List<RecipeHistory> viewStatusesWithCategory;

        @BeforeEach
        void beforeEach() {
          // 실제 도메인 객체들 생성
          RecipeHistory status1 = RecipeHistory.create(clock, UUID.randomUUID(), UUID.randomUUID());
          status1.updateRecipeCategoryId(categoryId);

          RecipeHistory status2 = RecipeHistory.create(clock, UUID.randomUUID(), UUID.randomUUID());
          status2.updateRecipeCategoryId(categoryId);

          viewStatusesWithCategory = List.of(status1, status2);

          doReturn(viewStatusesWithCategory)
              .when(repository)
              .findByRecipeCategoryIdAndStatus(categoryId, RecipeHistoryStatus.ACTIVE);
          doReturn(viewStatusesWithCategory).when(repository).saveAll(any());
        }

        @Test
        @DisplayName("Then - 해당 카테고리를 가진 모든 레시피 조회 상태의 카테고리가 비워져야 한다")
        public void thenShouldEmptyAllRecipeHistoryCategories() {
          service.unCategorize(categoryId);

          // 모든 상태의 카테고리가 null로 변경되었는지 확인
          assertThat(viewStatusesWithCategory)
              .allMatch(status -> status.getRecipeCategoryId() == null);

          verify(repository)
              .saveAll(
                  argThat(
                      statuses -> {
                        List<RecipeHistory> statusList = (List<RecipeHistory>) statuses;
                        return statusList.size() == 2
                            && statusList.stream()
                                .allMatch(status -> status.getRecipeCategoryId() == null);
                      }));
        }
      }

      @Nested
      @DisplayName("When - 해당 카테고리를 가진 레시피 조회 상태가 없다면")
      class WhenNoRecipeHistoryesWithCategoryExist {

        @BeforeEach
        void beforeEach() {
          doReturn(List.of())
              .when(repository)
              .findByRecipeCategoryIdAndStatus(categoryId, RecipeHistoryStatus.ACTIVE);
        }

        @Test
        @DisplayName("Then - saveAll이 빈 리스트로 호출되어야 한다")
        public void thenShouldCallSaveAllWithEmptyList() {
          service.unCategorize(categoryId);

          verify(repository)
              .findByRecipeCategoryIdAndStatus(categoryId, RecipeHistoryStatus.ACTIVE);
          verify(repository).saveAll(List.of());
        }
      }
    }
  }

  @Nested
  @DisplayName("카테고리별 레시피 조회 상태 조회")
  class FindCategorizedRecipeHistoryes {

    @Test
    @DisplayName("특정 카테고리의 레시피 조회 상태들을 조회한다")
    void shouldFindRecipeHistoryesByCategory() {
      UUID userId = UUID.randomUUID();
      UUID categoryId = UUID.randomUUID();
      int page = 0;
      Pageable pageable = RecipePageRequest.create(page, HistorySort.VIEWED_AT_DESC);

      Page<RecipeHistory> expectedStatuses =
          new PageImpl<>(List.of(RecipeHistory.create(clock, userId, UUID.randomUUID())));

      doReturn(expectedStatuses)
          .when(repository)
          .findAllByUserIdAndRecipeCategoryIdAndStatus(
              any(UUID.class),
              any(UUID.class),
              any(RecipeHistoryStatus.class),
              any(Pageable.class));

      Page<RecipeHistory> result = service.getCategorized(userId, categoryId, page);

      assertThat(result).isEqualTo(expectedStatuses);
      verify(repository)
          .findAllByUserIdAndRecipeCategoryIdAndStatus(
              userId, categoryId, RecipeHistoryStatus.ACTIVE, pageable);
    }
  }

  @Nested
  @DisplayName("미분류 레시피 조회 상태 조회")
  class FindUncategorizedRecipeHistoryes {

    @Test
    @DisplayName("카테고리가 없는 레시피 조회 상태들을 조회한다")
    void shouldFindUncategorizedRecipeHistoryes() {
      UUID userId = UUID.randomUUID();
      int page = 0;
      Pageable pageable = PageRequest.of(page, 10, HistorySort.VIEWED_AT_DESC);

      List<RecipeHistory> expectedStatuses =
          List.of(RecipeHistory.create(clock, userId, UUID.randomUUID()));
      Page<RecipeHistory> expectedPage =
          new PageImpl<>(expectedStatuses, pageable, expectedStatuses.size());

      doReturn(expectedPage)
          .when(repository)
          .findAllByUserIdAndRecipeCategoryIdAndStatus(
              any(UUID.class), isNull(), any(RecipeHistoryStatus.class), any(Pageable.class));

      Page<RecipeHistory> result = service.getUnCategorized(userId, page);

      assertThat(result.getContent()).isEqualTo(expectedStatuses);
      verify(repository)
          .findAllByUserIdAndRecipeCategoryIdAndStatus(
              userId, null, RecipeHistoryStatus.ACTIVE, pageable);
    }
  }

  @Nested
  @DisplayName("최근 조회한 레시피 상태 조회")
  class FindRecentRecipeHistoryes {

    @Nested
    @DisplayName("Given - 유효한 사용자 ID가 주어졌을 때")
    class GivenValidUserId {

      private UUID userId;
      private Integer page;
      private Pageable pageable;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        page = 0;
        pageable = RecipePageRequest.create(page, HistorySort.VIEWED_AT_DESC);
      }

      @Nested
      @DisplayName("When - 사용자의 최근 조회한 레시피 상태들을 조회한다면")
      class WhenFindingRecentRecipeHistoryes {

        private Page<RecipeHistory> expectedStatuses;

        @BeforeEach
        void beforeEach() {
          expectedStatuses =
              new PageImpl<>(
                  List.of(
                      RecipeHistory.create(clock, userId, UUID.randomUUID()),
                      RecipeHistory.create(clock, userId, UUID.randomUUID())));
          doReturn(expectedStatuses)
              .when(repository)
              .findByUserIdAndStatus(userId, RecipeHistoryStatus.ACTIVE, pageable);
        }

        @Test
        @DisplayName("Then - 최근 조회 순서로 정렬된 레시피 상태들이 반환되어야 한다")
        void thenShouldReturnRecentRecipeHistoryes() {
          List<RecipeHistory> result = service.getRecents(userId, page).getContent();

          assertThat(result).isEqualTo(expectedStatuses.getContent());
          verify(repository).findByUserIdAndStatus(userId, RecipeHistoryStatus.ACTIVE, pageable);
        }
      }
    }
  }

  @Nested
  @DisplayName("모든 레시피 히스토리 조회")
  class GetAllRecipeHistories {

    @Nested
    @DisplayName("Given - 유효한 사용자 ID가 주어졌을 때")
    class GivenValidUserId {

      private UUID userId;
      private Integer page;
      private Pageable pageable;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        page = 0;
        pageable = RecipePageRequest.create(page, HistorySort.VIEWED_AT_DESC);
      }

      @Nested
      @DisplayName("When - 사용자의 모든 레시피 히스토리를 조회한다면")
      class WhenGettingAllRecipeHistories {

        private Page<RecipeHistory> expectedHistories;

        @BeforeEach
        void beforeEach() {
          RecipeHistory history1 = RecipeHistory.create(clock, userId, UUID.randomUUID());
          RecipeHistory history2 = RecipeHistory.create(clock, userId, UUID.randomUUID());
          history2.updateRecipeCategoryId(UUID.randomUUID()); // 카테고리 있음

          RecipeHistory history3 = RecipeHistory.create(clock, userId, UUID.randomUUID());
          // 카테고리 없음

          expectedHistories = new PageImpl<>(List.of(history1, history2, history3));

          doReturn(expectedHistories)
              .when(repository)
              .findAllByUserIdAndStatus(userId, RecipeHistoryStatus.ACTIVE, pageable);
        }

        @Test
        @DisplayName("Then - 카테고리 유무와 상관없이 모든 히스토리를 반환해야 한다")
        void thenShouldReturnAllHistories() {
          Page<RecipeHistory> result = service.getAll(userId, page);

          assertThat(result.getContent()).hasSize(3);
          assertThat(result).isEqualTo(expectedHistories);
          verify(repository).findAllByUserIdAndStatus(userId, RecipeHistoryStatus.ACTIVE, pageable);
        }
      }
    }

    @Nested
    @DisplayName("Given - 히스토리가 없는 사용자 ID가 주어졌을 때")
    class GivenUserIdWithNoHistories {

      private UUID userId;
      private Integer page;
      private Pageable pageable;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        page = 0;
        pageable = RecipePageRequest.create(page, HistorySort.VIEWED_AT_DESC);
      }

      @Nested
      @DisplayName("When - 사용자의 모든 레시피 히스토리를 조회한다면")
      class WhenGettingAllRecipeHistories {

        @BeforeEach
        void beforeEach() {
          Page<RecipeHistory> emptyPage = new PageImpl<>(List.of());
          doReturn(emptyPage)
              .when(repository)
              .findAllByUserIdAndStatus(userId, RecipeHistoryStatus.ACTIVE, pageable);
        }

        @Test
        @DisplayName("Then - 빈 페이지를 반환해야 한다")
        void thenShouldReturnEmptyPage() {
          Page<RecipeHistory> result = service.getAll(userId, page);

          assertThat(result.getContent()).isEmpty();
          assertThat(result.getTotalElements()).isEqualTo(0);
          verify(repository).findAllByUserIdAndStatus(userId, RecipeHistoryStatus.ACTIVE, pageable);
        }
      }
    }
  }

  @Nested
  @DisplayName("카테고리별 레시피 개수 조회")
  class CountRecipeHistoryesByCategories {

    @Nested
    @DisplayName("Given - 유효한 카테고리 ID 목록이 주어졌을 때")
    class GivenValidCategoryIds {

      private List<UUID> categoryIds;

      @BeforeEach
      void setUp() {
        categoryIds = List.of(UUID.randomUUID(), UUID.randomUUID());
      }

      @Nested
      @DisplayName("When - 카테고리별 레시피 개수를 조회한다면")
      class WhenCountingRecipeHistoryesByCategories {

        private List<RecipeHistoryCategorizedCountProjection> projections;

        @BeforeEach
        void beforeEach() {
          projections =
              List.of(
                  createMockProjection(categoryIds.get(0), 5L),
                  createMockProjection(categoryIds.get(1), 3L));
          doReturn(projections)
              .when(repository)
              .countByCategoryIdsAndStatus(categoryIds, RecipeHistoryStatus.ACTIVE);
        }

        private RecipeHistoryCategorizedCountProjection createMockProjection(
            UUID categoryId, Long count) {
          RecipeHistoryCategorizedCountProjection projection =
              mock(RecipeHistoryCategorizedCountProjection.class);
          doReturn(categoryId).when(projection).getCategoryId();
          doReturn(count).when(projection).getCount();
          return projection;
        }

        @Test
        @DisplayName("Then - 각 카테고리별 레시피 개수가 반환되어야 한다")
        void thenShouldReturnRecipeHistoryCountsByCategories() {
          List<RecipeHistoryCategorizedCount> result = service.countByCategories(categoryIds);

          assertThat(result).hasSize(2);

          RecipeHistoryCategorizedCount first = result.get(0);
          assertThat(first.getCategoryId()).isEqualTo(categoryIds.get(0));
          assertThat(first.getCount()).isEqualTo(5);

          RecipeHistoryCategorizedCount second = result.get(1);
          assertThat(second.getCategoryId()).isEqualTo(categoryIds.get(1));
          assertThat(second.getCount()).isEqualTo(3);

          verify(repository).countByCategoryIdsAndStatus(categoryIds, RecipeHistoryStatus.ACTIVE);
        }
      }
    }
  }

  @Nested
  @DisplayName("사용자의 레시피 조회 상태 목록 조회")
  class GetUserRecipeHistoryes {

    @Nested
    @DisplayName("Given - 유효한 레시피 ID 목록과 사용자 ID가 주어졌을 때")
    class GivenValidRecipeIdsAndUserId {

      private List<UUID> recipeIds;
      private UUID userId;

      @BeforeEach
      void setUp() {
        recipeIds = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        userId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 사용자의 레시피 조회 상태 목록을 조회한다면")
      class WhenGettingUserRecipeHistoryes {

        private List<RecipeHistory> viewStatuses;

        @BeforeEach
        void beforeEach() {
          viewStatuses =
              List.of(
                  RecipeHistory.create(clock, userId, recipeIds.get(0)),
                  RecipeHistory.create(clock, userId, recipeIds.get(1)));

          doReturn(viewStatuses)
              .when(repository)
              .findByRecipeIdInAndUserIdAndStatus(recipeIds, userId, RecipeHistoryStatus.ACTIVE);
        }

        @Test
        @DisplayName("Then - 사용자가 조회한 레시피 상태 목록을 반환해야 한다")
        void thenShouldReturnUserRecipeHistoryes() {
          List<RecipeHistory> result = service.getByRecipes(recipeIds, userId);

          assertThat(result).hasSize(2);
          assertThat(result).containsExactlyElementsOf(viewStatuses);
          verify(repository)
              .findByRecipeIdInAndUserIdAndStatus(recipeIds, userId, RecipeHistoryStatus.ACTIVE);
        }
      }
    }

    @Nested
    @DisplayName("Given - 사용자가 조회하지 않은 레시피 ID 목록이 주어졌을 때")
    class GivenUnviewedRecipeIds {

      private List<UUID> recipeIds;
      private UUID userId;

      @BeforeEach
      void setUp() {
        recipeIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        userId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 사용자의 레시피 조회 상태 목록을 조회한다면")
      class WhenGettingUserRecipeHistoryes {

        @BeforeEach
        void beforeEach() {
          doReturn(List.of())
              .when(repository)
              .findByRecipeIdInAndUserIdAndStatus(recipeIds, userId, RecipeHistoryStatus.ACTIVE);
        }

        @Test
        @DisplayName("Then - 빈 목록을 반환해야 한다")
        void thenShouldReturnEmptyList() {
          List<RecipeHistory> result = service.getByRecipes(recipeIds, userId);

          assertThat(result).isEmpty();
          verify(repository)
              .findByRecipeIdInAndUserIdAndStatus(recipeIds, userId, RecipeHistoryStatus.ACTIVE);
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
      @DisplayName("When - 사용자의 레시피 조회 상태 목록을 조회한다면")
      class WhenGettingUserRecipeHistoryes {

        @BeforeEach
        void beforeEach() {
          doReturn(List.of())
              .when(repository)
              .findByRecipeIdInAndUserIdAndStatus(List.of(), userId, RecipeHistoryStatus.ACTIVE);
        }

        @Test
        @DisplayName("Then - 빈 목록을 반환해야 한다")
        void thenShouldReturnEmptyList() {
          List<RecipeHistory> result = service.getByRecipes(List.of(), userId);

          assertThat(result).isEmpty();
          verify(repository)
              .findByRecipeIdInAndUserIdAndStatus(List.of(), userId, RecipeHistoryStatus.ACTIVE);
        }
      }
    }

    @Nested
    @DisplayName("Given - 일부만 조회한 레시피 ID 목록이 주어졌을 때")
    class GivenPartiallyViewedRecipeIds {

      private List<UUID> recipeIds;
      private UUID userId;

      @BeforeEach
      void setUp() {
        recipeIds = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        userId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 사용자의 레시피 조회 상태 목록을 조회한다면")
      class WhenGettingUserRecipeHistoryes {

        private List<RecipeHistory> viewStatuses;

        @BeforeEach
        void beforeEach() {
          // 3개 중 2개만 조회한 상태
          viewStatuses =
              List.of(
                  RecipeHistory.create(clock, userId, recipeIds.get(0)),
                  RecipeHistory.create(clock, userId, recipeIds.get(2)));

          doReturn(viewStatuses)
              .when(repository)
              .findByRecipeIdInAndUserIdAndStatus(recipeIds, userId, RecipeHistoryStatus.ACTIVE);
        }

        @Test
        @DisplayName("Then - 조회한 레시피 상태만 반환해야 한다")
        void thenShouldReturnOnlyViewedRecipeStatuses() {
          List<RecipeHistory> result = service.getByRecipes(recipeIds, userId);

          assertThat(result).hasSize(2);
          assertThat(result.get(0).getRecipeId()).isEqualTo(recipeIds.get(0));
          assertThat(result.get(1).getRecipeId()).isEqualTo(recipeIds.get(2));
          verify(repository)
              .findByRecipeIdInAndUserIdAndStatus(recipeIds, userId, RecipeHistoryStatus.ACTIVE);
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 조회 상태 삭제")
  class DeleteRecipeHistory {

    @Nested
    @DisplayName("Given - 유효한 레시피 ID와 사용자 ID가 주어졌을 때")
    class GivenValidRecipeAndUserId {

      private UUID recipeId;
      private UUID userId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        userId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 레시피 조회 상태를 삭제한다면")
      class WhenDeletingRecipeHistory {

        private RecipeHistory realRecipeHistory;

        @BeforeEach
        void beforeEach() {
          realRecipeHistory = RecipeHistory.create(clock, userId, recipeId);
          doReturn(Optional.of(realRecipeHistory))
              .when(repository)
              .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeHistoryStatus.ACTIVE);
        }

        @Test
        @DisplayName("Then - 레시피 조회 상태가 삭제되어야 한다")
        public void thenShouldDeleteRecipeHistory() {
          service.delete(userId, recipeId);

          verify(repository)
              .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeHistoryStatus.ACTIVE);
          verify(repository).save(realRecipeHistory);
          assertThat(realRecipeHistory.getStatus() == RecipeHistoryStatus.DELETED).isTrue();
        }
      }
    }

    @Nested
    @DisplayName("Given - 존재하지 않는 레시피 ID와 사용자 ID가 주어졌을 떄")
    class GivenNonExistentRecipeAndUserId {

      private UUID recipeId;
      private UUID userId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        userId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 레시피 조회 상태를 삭제한다면")
      class WhenDeletingNonExistentRecipeHistory {

        @BeforeEach
        void beforeEach() {
          doReturn(Optional.empty())
              .when(repository)
              .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeHistoryStatus.ACTIVE);
        }

        @Test
        @DisplayName("Then - ViewStatusException이 발생해야 한다")
        public void thenShouldThrowViewStatusException() {
          RecipeHistoryException exception =
              assertThrows(
                  RecipeHistoryException.class,
                  () -> {
                    service.delete(userId, recipeId);
                  });

          assertThat(exception.getErrorMessage())
              .isEqualTo(RecipeHistoryErrorCode.RECIPE_HISTORY_NOT_FOUND);
          verify(repository, never()).save(any());
        }
      }
    }
  }

  @Nested
  @DisplayName("미분류 레시피 개수 조회")
  class CountUncategorized {

    @Nested
    @DisplayName("Given - 유효한 사용자 ID가 주어졌을 때")
    class GivenValidUserId {

      private UUID userId;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 미분류 레시피 개수를 조회한다면")
      class WhenCountingUncategorized {

        private RecipeHistoryUnCategorizedCountProjection projection;

        @BeforeEach
        void beforeEach() {
          projection = mock(RecipeHistoryUnCategorizedCountProjection.class);
          doReturn(5L).when(projection).getCount();
          doReturn(projection)
              .when(repository)
              .countByUserIdAndStatus(userId, RecipeHistoryStatus.ACTIVE);
        }

        @Test
        @DisplayName("Then - 올바른 미분류 레시피 개수를 반환해야 한다")
        void thenShouldReturnCorrectUncategorizedCount() {
          RecipeHistoryUnCategorizedCount result = service.countUncategorized(userId);

          assertThat(result.getCount()).isEqualTo(5);
          verify(repository).countByUserIdAndStatus(userId, RecipeHistoryStatus.ACTIVE);
        }
      }
    }

    @Nested
    @DisplayName("Given - 미분류 레시피가 없는 사용자 ID가 주어졌을 때")
    class GivenUserIdWithNoUncategorized {

      private UUID userId;

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 미분류 레시피 개수를 조회한다면")
      class WhenCountingUncategorized {

        private RecipeHistoryUnCategorizedCountProjection projection;

        @BeforeEach
        void beforeEach() {
          projection = mock(RecipeHistoryUnCategorizedCountProjection.class);
          doReturn(0L).when(projection).getCount();
          doReturn(projection)
              .when(repository)
              .countByUserIdAndStatus(userId, RecipeHistoryStatus.ACTIVE);
        }

        @Test
        @DisplayName("Then - 0을 반환해야 한다")
        void thenShouldReturnZero() {
          RecipeHistoryUnCategorizedCount result = service.countUncategorized(userId);

          assertThat(result.getCount()).isEqualTo(0);
          verify(repository).countByUserIdAndStatus(userId, RecipeHistoryStatus.ACTIVE);
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피별 히스토리 삭제")
  class DeleteByRecipe {

    @Nested
    @DisplayName("Given - 유효한 레시피 ID가 주어졌을 때")
    class GivenValidRecipeId {

      private UUID recipeId;
      private LocalDateTime fixedTime;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        doReturn(fixedTime).when(clock).now();
      }

      @Nested
      @DisplayName("When - 해당 레시피의 모든 히스토리를 삭제한다면")
      class WhenDeletingAllHistoriesByRecipe {

        private List<RecipeHistory> histories;

        @BeforeEach
        void beforeEach() {
          RecipeHistory history1 = RecipeHistory.create(clock, UUID.randomUUID(), recipeId);
          RecipeHistory history2 = RecipeHistory.create(clock, UUID.randomUUID(), recipeId);
          RecipeHistory history3 = RecipeHistory.create(clock, UUID.randomUUID(), recipeId);
          histories = List.of(history1, history2, history3);

          doReturn(histories).when(repository).findAllByRecipeId(recipeId);
          doReturn(histories).when(repository).saveAll(anyList());
        }

        @Test
        @DisplayName("Then - 모든 히스토리가 삭제 상태로 변경되어야 한다")
        void thenShouldDeleteAllHistories() {
          service.deleteByRecipe(recipeId);

          assertThat(histories)
              .allMatch(history -> history.getStatus() == RecipeHistoryStatus.DELETED);
          verify(repository).findAllByRecipeId(recipeId);
          verify(repository)
              .saveAll(
                  argThat(
                      savedHistories -> {
                        List<RecipeHistory> list = (List<RecipeHistory>) savedHistories;
                        return list.size() == 3
                            && list.stream()
                                .allMatch(h -> h.getStatus() == RecipeHistoryStatus.DELETED);
                      }));
        }
      }
    }

    @Nested
    @DisplayName("Given - 히스토리가 없는 레시피 ID가 주어졌을 때")
    class GivenRecipeIdWithNoHistories {

      private UUID recipeId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 해당 레시피의 모든 히스토리를 삭제한다면")
      class WhenDeletingAllHistoriesByRecipe {

        @BeforeEach
        void beforeEach() {
          doReturn(List.of()).when(repository).findAllByRecipeId(recipeId);
        }

        @Test
        @DisplayName("Then - 빈 리스트로 saveAll이 호출되어야 한다")
        void thenShouldSaveEmptyList() {
          service.deleteByRecipe(recipeId);

          verify(repository).findAllByRecipeId(recipeId);
          verify(repository).saveAll(List.of());
        }
      }
    }
  }
}
