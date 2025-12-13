package com.cheftory.api.recipeinfo.progress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.progress.entity.RecipeProgress;
import com.cheftory.api.recipeinfo.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipeinfo.progress.entity.RecipeProgressStep;
import com.cheftory.api.recipeinfo.progress.utils.RecipeProgressSort;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DisplayName("RecipeProgressRepository 테스트")
public class RecipeProgressRepositoryTest extends DbContextTest {

  @Autowired private RecipeProgressRepository recipeProgressRepository;

  @MockitoBean private Clock clock;

  private final LocalDateTime FIXED_TIME = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

  @BeforeEach
  void setUp() {
    doReturn(FIXED_TIME).when(clock).now();
  }

  @Nested
  @DisplayName("레시피 진행 상황 조회")
  class FindRecipeProgress {

    @Nested
    @DisplayName("Given - 유효한 레시피 ID가 주어졌을 때")
    class GivenValidRecipeId {

      private UUID recipeId;
      private RecipeProgressStep step;
      private RecipeProgressDetail detail;

      @BeforeEach
      void setUp() {

        recipeId = UUID.randomUUID();
        step = RecipeProgressStep.READY;
        detail = RecipeProgressDetail.READY;

        RecipeProgress recipeProgress = RecipeProgress.create(recipeId, clock, step, detail);
        recipeProgressRepository.save(recipeProgress);
      }

      @Nested
      @DisplayName("When - 레시피 진행 상황을 조회하면")
      class WhenFindRecipeProgress {
        List<RecipeProgress> results;

        @BeforeEach
        void setUp() {
          results =
              recipeProgressRepository.findAllByRecipeId(
                  recipeId, RecipeProgressSort.CREATE_AT_ASC);
        }

        @DisplayName("Then - 해당 레시피의 진행 상황이 반환된다")
        @Test
        void shouldReturnRecipeProgress() {

          assertThat(results).hasSize(1);
          assertThat(results.getFirst().getRecipeId()).isEqualTo(recipeId);
          assertThat(results.getFirst().getStep()).isEqualTo(step);
          assertThat(results.getFirst().getDetail()).isEqualTo(detail);
          assertThat(results.getFirst().getCreatedAt()).isEqualTo(FIXED_TIME);
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 진행 상황 정렬 조회")
  class FindRecipeProgressWithSort {

    @Nested
    @DisplayName("Given - 여러 개의 레시피 진행 상황이 시간 순서대로 저장되어 있을 때")
    class GivenMultipleRecipeProgressInTimeOrder {

      private UUID recipeId;
      private LocalDateTime firstTime;
      private LocalDateTime secondTime;
      private LocalDateTime thirdTime;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        firstTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        secondTime = LocalDateTime.of(2024, 1, 1, 11, 0, 0);
        thirdTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

        // 세 번째로 생성된 것 먼저 저장
        doReturn(thirdTime).when(clock).now();
        RecipeProgress thirdProgress =
            RecipeProgress.create(
                recipeId, clock, RecipeProgressStep.FINISHED, RecipeProgressDetail.FINISHED);
        recipeProgressRepository.save(thirdProgress);

        // 첫 번째로 생성된 것 저장
        doReturn(firstTime).when(clock).now();
        RecipeProgress firstProgress =
            RecipeProgress.create(
                recipeId, clock, RecipeProgressStep.READY, RecipeProgressDetail.READY);
        recipeProgressRepository.save(firstProgress);

        // 두 번째로 생성된 것 저장
        doReturn(secondTime).when(clock).now();
        RecipeProgress secondProgress =
            RecipeProgress.create(
                recipeId, clock, RecipeProgressStep.STEP, RecipeProgressDetail.STEP);
        recipeProgressRepository.save(secondProgress);
      }

      @Nested
      @DisplayName("When - createdAt 오름차순으로 정렬하여 조회하면")
      class WhenFindWithCreatedAtAscSort {
        List<RecipeProgress> results;

        @BeforeEach
        void setUp() {
          results =
              recipeProgressRepository.findAllByRecipeId(
                  recipeId, RecipeProgressSort.CREATE_AT_ASC);
        }

        @DisplayName("Then - 생성 시간 순서대로 정렬된 결과가 반환된다")
        @Test
        void shouldReturnRecipeProgressSortedByCreatedAtAsc() {
          assertThat(results).hasSize(3);

          // 첫 번째: 가장 이른 시간
          assertThat(results.get(0).getCreatedAt()).isEqualTo(firstTime);
          assertThat(results.get(0).getStep()).isEqualTo(RecipeProgressStep.READY);

          // 두 번째: 중간 시간
          assertThat(results.get(1).getCreatedAt()).isEqualTo(secondTime);
          assertThat(results.get(1).getStep()).isEqualTo(RecipeProgressStep.STEP);

          // 세 번째: 가장 늦은 시간
          assertThat(results.get(2).getCreatedAt()).isEqualTo(thirdTime);
          assertThat(results.get(2).getStep()).isEqualTo(RecipeProgressStep.FINISHED);
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 진행 상황 생성")
  class CreateRecipeProgress {

    @Nested
    @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
    class GivenValidParameters {

      private UUID recipeId;
      private RecipeProgressStep step;
      private RecipeProgressDetail detail;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        step = RecipeProgressStep.STEP;
        detail = RecipeProgressDetail.DETAIL_META;
      }

      @Nested
      @DisplayName("When - 레시피 진행 상황을 생성하면")
      class WhenCreateRecipeProgress {

        @BeforeEach
        void setUp() {
          RecipeProgress recipeProgress = RecipeProgress.create(recipeId, clock, step, detail);
          recipeProgressRepository.save(recipeProgress);
        }

        @DisplayName("Then - 레시피 진행 상황이 저장된다")
        @Test
        void shouldSaveRecipeProgress() {
          List<RecipeProgress> results =
              recipeProgressRepository.findAllByRecipeId(
                  recipeId, RecipeProgressSort.CREATE_AT_ASC);

          assertThat(results).hasSize(1);
          assertThat(results.getFirst().getRecipeId()).isEqualTo(recipeId);
          assertThat(results.getFirst().getStep()).isEqualTo(step);
          assertThat(results.getFirst().getDetail()).isEqualTo(detail);
          assertThat(results.getFirst().getCreatedAt()).isEqualTo(FIXED_TIME);
        }
      }
    }
  }
}
