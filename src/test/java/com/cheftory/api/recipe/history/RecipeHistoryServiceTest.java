package com.cheftory.api.recipe.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api._common.cursor.ViewedAtCursor;
import com.cheftory.api._common.cursor.ViewedAtCursorCodec;
import com.cheftory.api.recipe.history.entity.RecipeHistory;
import com.cheftory.api.recipe.history.entity.RecipeHistoryCategorizedCount;
import com.cheftory.api.recipe.history.entity.RecipeHistoryCategorizedCountProjection;
import com.cheftory.api.recipe.history.entity.RecipeHistoryStatus;
import com.cheftory.api.recipe.history.entity.RecipeHistoryUnCategorizedCount;
import com.cheftory.api.recipe.history.entity.RecipeHistoryUnCategorizedCountProjection;
import com.cheftory.api.recipe.history.exception.RecipeHistoryErrorCode;
import com.cheftory.api.recipe.history.exception.RecipeHistoryException;
import com.cheftory.api.recipe.history.utils.RecipeHistorySort;
import com.cheftory.api.recipe.util.RecipePageRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DisplayName("RecipeHistoryService Tests")
public class RecipeHistoryServiceTest {

  private RecipeHistoryRepository repository;
  private RecipeHistoryService service;
  private Clock clock;
  private ViewedAtCursorCodec viewedAtCursorCodec;

  @BeforeEach
  void setUp() {
    repository = mock(RecipeHistoryRepository.class);
    clock = mock(Clock.class);
    viewedAtCursorCodec = mock(ViewedAtCursorCodec.class);
    service = new RecipeHistoryService(repository, viewedAtCursorCodec, clock);
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

      @Test
      @DisplayName("Then - save가 호출되고 true를 반환해야 한다")
      void thenShouldCallSaveAndReturnTrue() {
        boolean result = service.create(userId, recipeId);

        assertThat(result).isTrue();

        verify(repository)
            .save(argThat(h -> h.getRecipeId().equals(recipeId) && h.getUserId().equals(userId)));

        verify(repository, never()).findByUserIdAndRecipeId(any(), any());
        verify(repository, never()).existsByUserIdAndRecipeId(any(), any());
        verify(repository, never()).existsByRecipeIdAndUserIdAndStatus(any(), any(), any());
      }
    }

    @Nested
    @DisplayName("Given - 이미 조회한 레시피 상태가 있을 때(중복 저장 상황)")
    class GivenExistingRecipeHistory {

      private UUID recipeId;
      private UUID userId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        userId = UUID.randomUUID();
      }

      @Test
      @DisplayName("Then - save에서 무결성 예외 발생 + 기존이 ACTIVE면 false를 반환해야 한다")
      void thenShouldReturnFalseWhenDuplicateAndActive() {
        doThrow(new DataIntegrityViolationException("duplicate"))
            .when(repository)
            .save(any(RecipeHistory.class));

        RecipeHistory existing = mock(RecipeHistory.class);
        doReturn(RecipeHistoryStatus.ACTIVE).when(existing).getStatus();
        doReturn(Optional.of(existing)).when(repository).findByUserIdAndRecipeId(userId, recipeId);

        boolean result = service.create(userId, recipeId);

        assertThat(result).isFalse();

        verify(repository).save(any(RecipeHistory.class));
        verify(repository).findByUserIdAndRecipeId(userId, recipeId);
        verify(repository, times(1)).save(any(RecipeHistory.class));
      }

      @Test
      @DisplayName("Then - save에서 무결성 예외 발생 + 기존이 BLOCKED면 false를 반환해야 한다")
      void thenShouldReturnFalseWhenDuplicateAndBlocked() {
        doThrow(new DataIntegrityViolationException("duplicate"))
            .when(repository)
            .save(any(RecipeHistory.class));

        RecipeHistory existing = mock(RecipeHistory.class);
        doReturn(RecipeHistoryStatus.BLOCKED).when(existing).getStatus();
        doReturn(Optional.of(existing)).when(repository).findByUserIdAndRecipeId(userId, recipeId);

        boolean result = service.create(userId, recipeId);

        assertThat(result).isFalse();

        verify(repository).save(any(RecipeHistory.class));
        verify(repository).findByUserIdAndRecipeId(userId, recipeId);
        verify(repository, times(1)).save(any(RecipeHistory.class));
      }

      @Test
      @DisplayName("Then - save에서 무결성 예외 발생 + 기존이 DELETED면 ACTIVE로 복구 후 true를 반환해야 한다")
      void thenShouldReactivateWhenDuplicateAndDeleted() {
        RecipeHistory deleted = mock(RecipeHistory.class);
        doReturn(RecipeHistoryStatus.DELETED).when(deleted).getStatus();
        doReturn(Optional.of(deleted)).when(repository).findByUserIdAndRecipeId(userId, recipeId);

        doThrow(new DataIntegrityViolationException("duplicate"))
            .doReturn(deleted)
            .when(repository)
            .save(any(RecipeHistory.class));

        boolean result = service.create(userId, recipeId);

        assertThat(result).isTrue();

        verify(repository).findByUserIdAndRecipeId(userId, recipeId);
        verify(deleted).active(clock);
        verify(repository, times(2)).save(any(RecipeHistory.class));
      }

      @Test
      @DisplayName("Then - save에서 무결성 예외 발생 + find 결과 없으면 예외를 다시 던져야 한다")
      void thenShouldRethrowWhenNotFoundExistingRow() {
        doThrow(new DataIntegrityViolationException("constraint"))
            .when(repository)
            .save(any(RecipeHistory.class));

        doReturn(Optional.empty()).when(repository).findByUserIdAndRecipeId(userId, recipeId);

        assertThrows(DataIntegrityViolationException.class, () -> service.create(userId, recipeId));

        verify(repository).save(any(RecipeHistory.class));
        verify(repository).findByUserIdAndRecipeId(userId, recipeId);
      }
    }
  }

  @Nested
  @DisplayName("커서 기반 조회")
  class CursorQueries {

    @Test
    @DisplayName("cursor가 비어 있으면 최근 기록 첫 페이지를 조회한다")
    void shouldGetRecentsFirstPageWithCursor() {
      UUID userId = UUID.randomUUID();
      UUID recipeId = UUID.randomUUID();
      LocalDateTime viewedAt = LocalDateTime.of(2024, 1, 1, 10, 0, 0);

      RecipeHistory history = mock(RecipeHistory.class);
      doReturn(viewedAt).when(history).getViewedAt();
      doReturn(recipeId).when(history).getId();

      List<RecipeHistory> rows =
          List.of(
              history, history, history, history, history, history, history, history, history,
              history, history, history, history, history, history, history, history, history,
              history, history, history, history);

      doReturn(rows)
          .when(repository)
          .findRecentsFirst(any(UUID.class), any(RecipeHistoryStatus.class), any(Pageable.class));
      doReturn("next-cursor")
          .when(viewedAtCursorCodec)
          .encode(new ViewedAtCursor(viewedAt, recipeId));

      CursorPage<RecipeHistory> result = service.getRecents(userId, null);

      assertThat(result.items()).hasSize(21);
      assertThat(result.nextCursor()).isEqualTo("next-cursor");
      verify(repository)
          .findRecentsFirst(eq(userId), eq(RecipeHistoryStatus.ACTIVE), any(Pageable.class));
    }

    @Test
    @DisplayName("cursor가 있으면 최근 기록 keyset을 조회한다")
    void shouldGetRecentsKeysetWithCursor() {
      UUID userId = UUID.randomUUID();
      UUID recipeId = UUID.randomUUID();
      LocalDateTime viewedAt = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
      ViewedAtCursor decoded = new ViewedAtCursor(viewedAt, recipeId);

      RecipeHistory history = mock(RecipeHistory.class);
      doReturn(viewedAt.minusMinutes(1)).when(history).getViewedAt();
      doReturn(UUID.randomUUID()).when(history).getId();

      doReturn(decoded).when(viewedAtCursorCodec).decode("cursor");
      doReturn(List.of(history))
          .when(repository)
          .findRecentsKeyset(
              eq(userId),
              eq(RecipeHistoryStatus.ACTIVE),
              eq(viewedAt),
              eq(recipeId),
              any(Pageable.class));

      CursorPage<RecipeHistory> result = service.getRecents(userId, "cursor");

      assertThat(result.items()).hasSize(1);
      verify(repository)
          .findRecentsKeyset(
              eq(userId),
              eq(RecipeHistoryStatus.ACTIVE),
              eq(viewedAt),
              eq(recipeId),
              any(Pageable.class));
    }

    @Test
    @DisplayName("커서 기반 카테고리 조회는 keyset을 사용한다")
    void shouldGetCategorizedKeysetWithCursor() {
      UUID userId = UUID.randomUUID();
      UUID categoryId = UUID.randomUUID();
      UUID recipeId = UUID.randomUUID();
      LocalDateTime viewedAt = LocalDateTime.of(2024, 1, 2, 10, 0, 0);
      ViewedAtCursor decoded = new ViewedAtCursor(viewedAt, recipeId);

      RecipeHistory history = mock(RecipeHistory.class);
      doReturn(viewedAt.minusMinutes(1)).when(history).getViewedAt();
      doReturn(UUID.randomUUID()).when(history).getId();

      doReturn(decoded).when(viewedAtCursorCodec).decode("cursor");
      doReturn(List.of(history))
          .when(repository)
          .findCategorizedKeyset(
              eq(userId),
              eq(categoryId),
              eq(RecipeHistoryStatus.ACTIVE),
              eq(viewedAt),
              eq(recipeId),
              any(Pageable.class));

      CursorPage<RecipeHistory> result = service.getCategorized(userId, categoryId, "cursor");

      assertThat(result.items()).hasSize(1);
      verify(repository)
          .findCategorizedKeyset(
              eq(userId),
              eq(categoryId),
              eq(RecipeHistoryStatus.ACTIVE),
              eq(viewedAt),
              eq(recipeId),
              any(Pageable.class));
    }

    @Test
    @DisplayName("커서 기반 미분류 조회는 keyset을 사용한다")
    void shouldGetUncategorizedKeysetWithCursor() {
      UUID userId = UUID.randomUUID();
      UUID recipeId = UUID.randomUUID();
      LocalDateTime viewedAt = LocalDateTime.of(2024, 1, 3, 10, 0, 0);
      ViewedAtCursor decoded = new ViewedAtCursor(viewedAt, recipeId);

      RecipeHistory history = mock(RecipeHistory.class);
      doReturn(viewedAt.minusMinutes(1)).when(history).getViewedAt();
      doReturn(UUID.randomUUID()).when(history).getId();

      doReturn(decoded).when(viewedAtCursorCodec).decode("cursor");
      doReturn(List.of(history))
          .when(repository)
          .findUncategorizedKeyset(
              eq(userId),
              eq(RecipeHistoryStatus.ACTIVE),
              eq(viewedAt),
              eq(recipeId),
              any(Pageable.class));

      CursorPage<RecipeHistory> result = service.getUnCategorized(userId, "cursor");

      assertThat(result.items()).hasSize(1);
      verify(repository)
          .findUncategorizedKeyset(
              eq(userId),
              eq(RecipeHistoryStatus.ACTIVE),
              eq(viewedAt),
              eq(recipeId),
              any(Pageable.class));
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
        @DisplayName("Then - getWithView 메서드는 viewedAt을 업데이트하고 반환해야 한다")
        public void thenShouldReturnCorrectRecipeHistory() {
          RecipeHistory status = service.getWithView(userId, recipeId);

          assertThat(status).isNotNull();
          assertThat(status.getRecipeId()).isEqualTo(recipeId);
          assertThat(status.getUserId()).isEqualTo(userId);
          assertThat(status.getViewedAt()).isEqualTo(updateTime);

          verify(repository)
              .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeHistoryStatus.ACTIVE);
          verify(repository).save(realRecipeHistory);
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 조회 상태 카테고리 변경")
  class ChangeRecipeHistoryRecipeCategory {

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
      class WhenChangingRecipeHistoryRecipeCategory {

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
                      h ->
                          h.getRecipeId().equals(recipeId)
                              && h.getUserId().equals(userId)
                              && h.getRecipeCategoryId().equals(newCategoryId)));
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
      class WhenChangingNonExistentRecipeHistoryRecipeCategory {

        @BeforeEach
        void beforeEach() {
          doReturn(Optional.empty())
              .when(repository)
              .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeHistoryStatus.ACTIVE);
        }

        @Test
        @DisplayName("Then - RecipeHistoryException이 발생해야 한다")
        public void thenShouldThrowViewStatusException() {
          RecipeHistoryException exception =
              assertThrows(
                  RecipeHistoryException.class,
                  () -> service.updateCategory(userId, recipeId, newCategoryId));

          assertThat(exception.getErrorMessage())
              .isEqualTo(RecipeHistoryErrorCode.RECIPE_HISTORY_NOT_FOUND);
          verify(repository, never()).save(any());
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 조회 상태 카테고리 삭제")
  class DeleteRecipeHistoryRecipeCategory {

    @Nested
    @DisplayName("Given - 삭제할 카테고리 ID가 주어졌을 때")
    class GivenRecipeCategoryIdToDelete {

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
      class WhenRecipeHistoryesWithRecipeCategoryExist {

        private List<RecipeHistory> viewStatusesWithCategory;

        @BeforeEach
        void beforeEach() {
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

          assertThat(viewStatusesWithCategory)
              .allMatch(status -> status.getRecipeCategoryId() == null);

          verify(repository)
              .saveAll(
                  argThat(
                      statuses -> {
                        List<RecipeHistory> statusList = (List<RecipeHistory>) statuses;
                        return statusList.size() == 2
                            && statusList.stream().allMatch(s -> s.getRecipeCategoryId() == null);
                      }));
        }
      }

      @Nested
      @DisplayName("When - 해당 카테고리를 가진 레시피 조회 상태가 없다면")
      class WhenNoRecipeHistoryesWithRecipeCategoryExist {

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
      Pageable pageable = RecipePageRequest.create(page, RecipeHistorySort.VIEWED_AT_DESC);

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
      Pageable pageable = PageRequest.of(page, 10, RecipeHistorySort.VIEWED_AT_DESC);

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
        pageable = RecipePageRequest.create(page, RecipeHistorySort.VIEWED_AT_DESC);
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
  @DisplayName("카테고리별 레시피 개수 조회")
  class CountRecipeHistoryesByCategories {

    @Nested
    @DisplayName("Given - 유효한 카테고리 ID 목록이 주어졌을 때")
    class GivenValidRecipeCategoryIds {

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
        @DisplayName("Then - RecipeHistoryException이 발생해야 한다")
        public void thenShouldThrowViewStatusException() {
          RecipeHistoryException exception =
              assertThrows(RecipeHistoryException.class, () -> service.delete(userId, recipeId));

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

          doReturn(histories)
              .when(repository)
              .findAllByRecipeIdAndStatus(recipeId, RecipeHistoryStatus.ACTIVE);

          doReturn(histories).when(repository).saveAll(anyList());
        }

        @Test
        @DisplayName("Then - 모든 히스토리가 삭제 상태로 변경되어야 한다")
        void thenShouldDeleteAllHistories() {
          List<RecipeHistory> result = service.deleteByRecipe(recipeId);

          assertThat(result).hasSize(3);
          assertThat(histories)
              .allMatch(history -> history.getStatus() == RecipeHistoryStatus.DELETED);

          verify(repository).findAllByRecipeIdAndStatus(recipeId, RecipeHistoryStatus.ACTIVE);
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
          doReturn(List.of())
              .when(repository)
              .findAllByRecipeIdAndStatus(recipeId, RecipeHistoryStatus.ACTIVE);
        }

        @Test
        @DisplayName("Then - 빈 리스트로 saveAll이 호출되어야 한다")
        void thenShouldSaveEmptyList() {
          List<RecipeHistory> result = service.deleteByRecipe(recipeId);

          assertThat(result).isEmpty();
          verify(repository).findAllByRecipeIdAndStatus(recipeId, RecipeHistoryStatus.ACTIVE);
          verify(repository).saveAll(List.of());
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피별 히스토리 차단")
  class BlockByRecipe {

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
      @DisplayName("When - 해당 레시피의 모든 히스토리를 차단한다면")
      class WhenBlockingAllHistoriesByRecipe {

        private List<RecipeHistory> histories;

        @BeforeEach
        void beforeEach() {
          RecipeHistory history1 = RecipeHistory.create(clock, UUID.randomUUID(), recipeId);
          RecipeHistory history2 = RecipeHistory.create(clock, UUID.randomUUID(), recipeId);
          histories = List.of(history1, history2);

          doReturn(histories).when(repository).findAllByRecipeId(recipeId);
          doReturn(histories).when(repository).saveAll(anyList());
        }

        @Test
        @DisplayName("Then - 모든 히스토리가 BLOCKED 상태로 변경되어야 한다")
        void thenShouldBlockAllHistories() {
          service.blockByRecipe(recipeId);

          assertThat(histories).allMatch(h -> h.getStatus() == RecipeHistoryStatus.BLOCKED);
          verify(repository).findAllByRecipeId(recipeId);
          verify(repository)
              .saveAll(
                  argThat(
                      saved -> {
                        List<RecipeHistory> list = (List<RecipeHistory>) saved;
                        return list.size() == 2
                            && list.stream()
                                .allMatch(h -> h.getStatus() == RecipeHistoryStatus.BLOCKED);
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
        doReturn(List.of()).when(repository).findAllByRecipeId(recipeId);
      }

      @Nested
      @DisplayName("When - 해당 레시피의 모든 히스토리를 차단한다면")
      class WhenBlockingAllHistoriesByRecipe {

        @Test
        @DisplayName("Then - 빈 리스트로 saveAll이 호출되어야 한다")
        void thenShouldSaveEmptyList() {
          service.blockByRecipe(recipeId);

          verify(repository).findAllByRecipeId(recipeId);
          verify(repository).saveAll(List.of());
        }
      }
    }
  }
}
