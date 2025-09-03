package com.cheftory.api.recipe.viewstatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.util.RecipePageRequest;
import com.cheftory.api.recipe.viewstatus.exception.ViewStatusErrorCode;
import com.cheftory.api.recipe.viewstatus.exception.ViewStatusException;
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

@DisplayName("RecipeViewStatusService Tests")
public class RecipeViewStatusServiceTest {

  private RecipeViewStatusRepository repository;
  private RecipeViewStatusService service;
  private Clock clock;

  @BeforeEach
  void setUp() {
    repository = mock(RecipeViewStatusRepository.class);
    clock = mock(Clock.class);
    service = new RecipeViewStatusService(repository, clock);
  }

  @Nested
  @DisplayName("레시피 조회 상태 생성")
  class CreateRecipeViewStatus {

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
      class WhenCreatingRecipeViewStatus {

        @BeforeEach
        void beforeEach() {
          doReturn(false).when(repository).existsByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeViewState.ACTIVE);
        }

        @Test
        @DisplayName("Then - 올바른 파라미터로 Repository의 save가 호출되어야 한다")
        public void thenShouldCallRepositorySaveWithCorrectParameters() {
          service.create(userId, recipeId);

          verify(repository).existsByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeViewState.ACTIVE);
          verify(repository).save(argThat(recipeViewStatus ->
              recipeViewStatus.getRecipeId().equals(recipeId)
                  && recipeViewStatus.getUserId().equals(userId)
          ));
        }
      }
    }

    @Nested
    @DisplayName("Given - 이미 조회한 레시피 상태가 있을 때")
    class GivenExistingRecipeViewStatus {

      private UUID recipeId;
      private UUID userId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        userId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 레시피 조회 상태를 생성한다면")
      class WhenCreatingExistingRecipeViewStatus {

        @BeforeEach
        void beforeEach() {
          doReturn(true).when(repository).existsByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeViewState.ACTIVE);
        }

        @Test
        @DisplayName("Then - 아무일도 일어나지 않아야 한다")
        public void thenShouldNotDoAnything() {
          service.create(userId, recipeId);

          verify(repository, never()).save(any(RecipeViewStatus.class));
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 조회 상태 조회")
  class FindRecipeViewStatus {

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
      class WhenFindingRecipeViewStatus {

        private RecipeViewStatus realRecipeViewStatus;

        @BeforeEach
        void beforeEach() {
          doReturn(initialTime).when(clock).now();
          realRecipeViewStatus = RecipeViewStatus.create(clock, userId, recipeId);

          doReturn(updateTime).when(clock).now();

          doReturn(Optional.of(realRecipeViewStatus)).when(repository).findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeViewState.ACTIVE);
          doReturn(realRecipeViewStatus).when(repository).save(any(RecipeViewStatus.class));
        }

        @Test
        @DisplayName("Then - 올바른 레시피 조회 상태가 반환되어야 한다")
        public void thenShouldReturnCorrectRecipeViewStatus() {
          RecipeViewStatus status = service.find(userId, recipeId);

          assertThat(status).isNotNull();
          assertThat(status.getRecipeId()).isEqualTo(recipeId);
          assertThat(status.getUserId()).isEqualTo(userId);
          assertThat(status.getViewedAt()).isEqualTo(updateTime); // 실제로 업데이트된 시간

          verify(repository).findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeViewState.ACTIVE);
          verify(repository).save(realRecipeViewStatus);
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
      class WhenFindingNonExistentRecipeViewStatus {

        @BeforeEach
        void beforeEach() {
          doReturn(Optional.empty()).when(repository).findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeViewState.ACTIVE);
        }

        @Test
        @DisplayName("Then - ViewStatusException이 발생해야 한다")
        public void thenShouldThrowViewStatusException() {
          ViewStatusException exception = assertThrows(ViewStatusException.class, () -> {
            service.find(userId, recipeId);
          });

          assertThat(exception.getErrorMessage()).isEqualTo(ViewStatusErrorCode.VIEW_STATUS_NOT_FOUND);
          verify(repository, never()).save(any());
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 조회 상태 카테고리 변경")
  class ChangeRecipeViewStatusCategory {

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
      class WhenChangingRecipeViewStatusCategory {

        private RecipeViewStatus realRecipeViewStatus;

        @BeforeEach
        void beforeEach() {
          realRecipeViewStatus = RecipeViewStatus.create(clock, userId, recipeId);
          doReturn(Optional.of(realRecipeViewStatus)).when(repository)
              .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeViewState.ACTIVE);
          doReturn(realRecipeViewStatus).when(repository).save(any(RecipeViewStatus.class));
        }

        @Test
        @DisplayName("Then - 카테고리가 올바르게 변경되어야 한다")
        public void thenShouldChangeCategoryCorrectly() {
          service.updateCategory(userId, recipeId, newCategoryId);

          assertThat(realRecipeViewStatus.getRecipeCategoryId()).isEqualTo(newCategoryId);
          verify(repository).save(argThat(recipeViewStatus ->
              recipeViewStatus.getRecipeId().equals(recipeId)
                  && recipeViewStatus.getUserId().equals(userId)
                  && recipeViewStatus.getRecipeCategoryId().equals(newCategoryId)
          ));
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
      class WhenChangingNonExistentRecipeViewStatusCategory {

        @BeforeEach
        void beforeEach() {
          doReturn(Optional.empty()).when(repository).findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeViewState.ACTIVE);
        }

        @Test
        @DisplayName("Then - ViewStatusException이 발생해야 한다")
        public void thenShouldThrowViewStatusException() {
          ViewStatusException exception = assertThrows(ViewStatusException.class, () -> {
            service.updateCategory(userId, recipeId, newCategoryId);
          });

          assertThat(exception.getErrorMessage()).isEqualTo(ViewStatusErrorCode.VIEW_STATUS_NOT_FOUND);
          verify(repository, never()).save(any());
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 조회 상태 카테고리 삭제")
  class DeleteRecipeViewStatusCategory {

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
      class WhenRecipeViewStatusesWithCategoryExist {

        private List<RecipeViewStatus> viewStatusesWithCategory;

        @BeforeEach
        void beforeEach() {
          // 실제 도메인 객체들 생성
          RecipeViewStatus status1 = RecipeViewStatus.create(clock, UUID.randomUUID(), UUID.randomUUID());
          status1.updateRecipeCategoryId(categoryId);

          RecipeViewStatus status2 = RecipeViewStatus.create(clock, UUID.randomUUID(), UUID.randomUUID());
          status2.updateRecipeCategoryId(categoryId);

          viewStatusesWithCategory = List.of(status1, status2);

          doReturn(viewStatusesWithCategory).when(repository).findByRecipeCategoryIdAndStatus(categoryId, RecipeViewState.ACTIVE);
          doReturn(viewStatusesWithCategory).when(repository).saveAll(any());
        }

        @Test
        @DisplayName("Then - 해당 카테고리를 가진 모든 레시피 조회 상태의 카테고리가 비워져야 한다")
        public void thenShouldEmptyAllRecipeViewStatusCategories() {
          service.deleteCategories(categoryId);

          // 모든 상태의 카테고리가 null로 변경되었는지 확인
          assertThat(viewStatusesWithCategory)
              .allMatch(status -> status.getRecipeCategoryId() == null);

          verify(repository).saveAll(argThat(statuses -> {
            List<RecipeViewStatus> statusList = (List<RecipeViewStatus>) statuses;
            return statusList.size() == 2 &&
                statusList.stream().allMatch(status -> status.getRecipeCategoryId() == null);
          }));
        }
      }

      @Nested
      @DisplayName("When - 해당 카테고리를 가진 레시피 조회 상태가 없다면")
      class WhenNoRecipeViewStatusesWithCategoryExist {

        @BeforeEach
        void beforeEach() {
          doReturn(List.of()).when(repository).findByRecipeCategoryIdAndStatus(categoryId, RecipeViewState.ACTIVE);
        }

        @Test
        @DisplayName("Then - saveAll이 빈 리스트로 호출되어야 한다")
        public void thenShouldCallSaveAllWithEmptyList() {
          service.deleteCategories(categoryId);

          verify(repository).findByRecipeCategoryIdAndStatus(categoryId, RecipeViewState.ACTIVE);
          verify(repository).saveAll(List.of());
        }
      }
    }
  }


  @Nested
  @DisplayName("카테고리별 레시피 조회 상태 조회")
  class FindCategorizedRecipeViewStatuses {

    @Test
    @DisplayName("특정 카테고리의 레시피 조회 상태들을 조회한다")
    void shouldFindRecipeViewStatusesByCategory() {
      UUID userId = UUID.randomUUID();
      UUID categoryId = UUID.randomUUID();
      int page = 0;
      Pageable pageable = RecipePageRequest.create(page, ViewStatusSort.VIEWED_AT_DESC);

      Page<RecipeViewStatus> expectedStatuses = new PageImpl<>(List.of(
          RecipeViewStatus.create(clock, userId, UUID.randomUUID())
      ));

      doReturn(expectedStatuses).when(repository)
          .findAllByUserIdAndRecipeCategoryIdAndStatus(
              any(UUID.class),
              any(UUID.class),
              any(RecipeViewState.class),
              any(Pageable.class)
          );

      Page<RecipeViewStatus> result = service.findCategories(userId,categoryId, page);

      assertThat(result).isEqualTo(expectedStatuses);
      verify(repository).findAllByUserIdAndRecipeCategoryIdAndStatus(userId, categoryId, RecipeViewState.ACTIVE, pageable);
    }
  }

  @Nested
  @DisplayName("미분류 레시피 조회 상태 조회")
  class FindUncategorizedRecipeViewStatuses {

    @Test
    @DisplayName("카테고리가 없는 레시피 조회 상태들을 조회한다")
    void shouldFindUncategorizedRecipeViewStatuses() {
      UUID userId = UUID.randomUUID();
      int page = 0;
      Pageable pageable = PageRequest.of(page, 10, ViewStatusSort.VIEWED_AT_DESC);

      List<RecipeViewStatus> expectedStatuses = List.of(
          RecipeViewStatus.create(clock, userId, UUID.randomUUID())
      );
      Page<RecipeViewStatus> expectedPage =
          new PageImpl<>(expectedStatuses, pageable, expectedStatuses.size());

      doReturn(expectedPage).when(repository)
          .findAllByUserIdAndRecipeCategoryIdAndStatus(
              any(UUID.class),
              isNull(),
              any(RecipeViewState.class),
              any(Pageable.class)
          );

      Page<RecipeViewStatus> result = service.findUnCategories(userId, page);

      assertThat(result.getContent()).isEqualTo(expectedStatuses);
      verify(repository).findAllByUserIdAndRecipeCategoryIdAndStatus(userId, null, RecipeViewState.ACTIVE, pageable);
    }
  }

  @Nested
  @DisplayName("최근 조회한 레시피 상태 조회")
  class FindRecentRecipeViewStatuses {

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
        pageable = RecipePageRequest.create(page, ViewStatusSort.VIEWED_AT_DESC);
      }

      @Nested
      @DisplayName("When - 사용자의 최근 조회한 레시피 상태들을 조회한다면")
      class WhenFindingRecentRecipeViewStatuses {

        private Page<RecipeViewStatus> expectedStatuses;;

        @BeforeEach
        void beforeEach() {
          expectedStatuses = new PageImpl<>(List.of(
              RecipeViewStatus.create(clock, userId, UUID.randomUUID()),
              RecipeViewStatus.create(clock, userId, UUID.randomUUID())
          ));
          doReturn(expectedStatuses).when(repository).findByUserIdAndStatus(userId, RecipeViewState.ACTIVE, pageable);
        }

        @Test
        @DisplayName("Then - 최근 조회 순서로 정렬된 레시피 상태들이 반환되어야 한다")
        void thenShouldReturnRecentRecipeViewStatuses() {
          List<RecipeViewStatus> result = service.findRecentUsers(userId, page).getContent();

          assertThat(result).isEqualTo(expectedStatuses.getContent());
          verify(repository).findByUserIdAndStatus(userId, RecipeViewState.ACTIVE, pageable);
        }
      }
    }
  }

  @Nested
  @DisplayName("카테고리별 레시피 개수 조회")
  class CountRecipeViewStatusesByCategories {

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
      class WhenCountingRecipeViewStatusesByCategories {

        private List<RecipeViewStatusCountProjection> projections;

        @BeforeEach
        void beforeEach() {
          projections = List.of(
              createMockProjection(categoryIds.get(0), 5L),
              createMockProjection(categoryIds.get(1), 3L)
          );
          doReturn(projections).when(repository).countByCategoryIdsAndStatus(categoryIds, RecipeViewState.ACTIVE);
        }

        private RecipeViewStatusCountProjection createMockProjection(UUID categoryId, Long count) {
          RecipeViewStatusCountProjection projection = mock(RecipeViewStatusCountProjection.class);
          doReturn(categoryId).when(projection).getCategoryId();
          doReturn(count).when(projection).getCount();
          return projection;
        }

        @Test
        @DisplayName("Then - 각 카테고리별 레시피 개수가 반환되어야 한다")
        void thenShouldReturnRecipeViewStatusCountsByCategories() {
          List<RecipeViewStatusCount> result = service.countByCategories(categoryIds);

          assertThat(result).hasSize(2);

          RecipeViewStatusCount first = result.get(0);
          assertThat(first.getCategoryId()).isEqualTo(categoryIds.get(0));
          assertThat(first.getCount()).isEqualTo(5);

          RecipeViewStatusCount second = result.get(1);
          assertThat(second.getCategoryId()).isEqualTo(categoryIds.get(1));
          assertThat(second.getCount()).isEqualTo(3);

          verify(repository).countByCategoryIdsAndStatus(categoryIds , RecipeViewState.ACTIVE);
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 조회 상태 삭제")
  class DeleteRecipeViewStatus {

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
      class WhenDeletingRecipeViewStatus {

        private RecipeViewStatus realRecipeViewStatus;

        @BeforeEach
        void beforeEach() {
          realRecipeViewStatus = RecipeViewStatus.create(clock, userId, recipeId);
          doReturn(Optional.of(realRecipeViewStatus)).when(repository)
              .findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeViewState.ACTIVE);
        }

        @Test
        @DisplayName("Then - 레시피 조회 상태가 삭제되어야 한다")
        public void thenShouldDeleteRecipeViewStatus() {
          service.delete(userId, recipeId);

          verify(repository).findByRecipeIdAndUserIdAndStatus(recipeId, userId,
              RecipeViewState.ACTIVE);
          verify(repository).save(realRecipeViewStatus);
          assertThat(realRecipeViewStatus.getStatus() == RecipeViewState.DELETED).isTrue();
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
      class WhenDeletingNonExistentRecipeViewStatus {

        @BeforeEach
        void beforeEach() {
          doReturn(Optional.empty()).when(repository).findByRecipeIdAndUserIdAndStatus(recipeId, userId, RecipeViewState.ACTIVE);
        }

        @Test
        @DisplayName("Then - ViewStatusException이 발생해야 한다")
        public void thenShouldThrowViewStatusException() {
          ViewStatusException exception = assertThrows(ViewStatusException.class, () -> {
            service.delete(userId, recipeId);
          });

          assertThat(exception.getErrorMessage()).isEqualTo(ViewStatusErrorCode.VIEW_STATUS_NOT_FOUND);
          verify(repository, never()).save(any());
        }
      }
    }
  }
}