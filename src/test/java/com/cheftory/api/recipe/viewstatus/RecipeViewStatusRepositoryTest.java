package com.cheftory.api.recipe.viewstatus;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DisplayName("RecipeViewStatusRepository Tests")
public class RecipeViewStatusRepositoryTest extends DbContextTest {

  @Autowired
  private RecipeViewStatusRepository repository;
  @MockitoBean
  private Clock clock;

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
          RecipeViewStatus savedStatus = repository.findById(recipeViewStatus.getId())
              .orElseThrow();
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
          boolean exists = repository.existsByRecipeIdAndUserId(recipeId, userId);
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
          boolean exists = repository.existsByRecipeIdAndUserId(recipeId, userId);
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
          RecipeViewStatus foundStatus = repository.findByRecipeIdAndUserId(recipeId, userId)
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
          assertThat(repository.findByRecipeIdAndUserId(recipeId, userId)).isEmpty();
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

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        recipeId = UUID.randomUUID();
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
          var statuses = repository.findAllByUserIdAndRecipeCategoryId(userId, null);

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

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 미분류 레시피 조회 상태를 가져온다면")
      class WhenFindingUncategorized {

        @Test
        @DisplayName("Then - 빈 결과를 반환해야 한다")
        void thenShouldReturnEmpty() {
          var statuses = repository.findAllByUserIdAndRecipeCategoryId(userId, null);
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

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        recipeId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
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
          var statuses = repository.findAllByUserIdAndRecipeCategoryId(userId, categoryId);

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

      @BeforeEach
      void setUp() {
        userId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 분류된 레시피 조회 상태를 가져온다면")
      class WhenFindingCategorized {

        @Test
        @DisplayName("Then - 빈 결과를 반환해야 한다")
        void thenShouldReturnEmpty() {
          var statuses = repository.findAllByUserIdAndRecipeCategoryId(userId, categoryId);
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
      @DisplayName("When - 사용자의 최근 조회 레시피 상태를 가져온다면")
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
          var statuses = repository.findByUserId(userId, ViewStatusSort.VIEWED_AT_DESC);

          assertThat(statuses.size()).isEqualTo(2);
          assertThat(statuses.getFirst().getUserId()).isEqualTo(userId);

          // 더 최근 것(11시)이 먼저 나와야 함
          assertThat(statuses.getFirst().getRecipeId()).isEqualTo(secondRecipeViewStatus.getRecipeId());
          assertThat(statuses.getLast().getRecipeId()).isEqualTo(firstRecipeViewStatus.getRecipeId());

          // 시간 검증 - 첫 번째가 더 최근이어야 함
          assertThat(statuses.getFirst().getViewedAt()).isAfter(statuses.getLast().getViewedAt());
        }
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
              .mapToObj(i -> {
                RecipeViewStatus status = RecipeViewStatus.create(clock, userId, UUID.randomUUID());
                status.updateRecipeCategoryId(categoryId1);
                return status;
              })
              .forEach(repository::save);

          IntStream.range(0, 2)
              .mapToObj(i -> {
                RecipeViewStatus status = RecipeViewStatus.create(clock, userId, UUID.randomUUID());
                status.updateRecipeCategoryId(categoryId2);
                return status;
              })
              .forEach(repository::save);
        }

        @Test
        @DisplayName("Then - 각 카테고리별 정확한 개수를 반환해야 한다")
        void thenShouldReturnCorrectCountsForEachCategory() {
          List<RecipeViewStatusCountProjection> counts = repository.countByCategoryIds(categoryIds);

          assertThat(counts.size()).isEqualTo(2);

          RecipeViewStatusCountProjection count1 = counts.stream()
              .filter(c -> c.getCategoryId().equals(categoryId1))
              .findFirst()
              .orElseThrow();
          assertThat(count1.getCount()).isEqualTo(3);

          RecipeViewStatusCountProjection count2 = counts.stream()
              .filter(c -> c.getCategoryId().equals(categoryId2))
              .findFirst()
              .orElseThrow();
          assertThat(count2.getCount()).isEqualTo(2);

          boolean hasCategory3 = counts.stream()
              .anyMatch(c -> c.getCategoryId().equals(categoryId3));
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
          List<RecipeViewStatusCountProjection> counts = repository.countByCategoryIds(nonExistentCategoryIds);
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
          List<RecipeViewStatusCountProjection> counts = repository.countByCategoryIds(List.of());
          assertThat(counts.size()).isEqualTo(0);
        }
      }
    }
  }
}