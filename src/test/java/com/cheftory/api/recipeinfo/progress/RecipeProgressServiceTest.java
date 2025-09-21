package com.cheftory.api.recipeinfo.progress;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api._common.Clock;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Recipe Progress Service Test")
public class RecipeProgressServiceTest {

  private RecipeProgressService recipeProgressService;
  private RecipeProgressRepository recipeProgressRepository;
  private Clock clock;

  @BeforeEach
  public void setUp() {
    clock = mock(Clock.class);
    recipeProgressRepository = mock(RecipeProgressRepository.class);
    recipeProgressService = new RecipeProgressService(recipeProgressRepository, clock);
  }

  @Nested
  @DisplayName("레시피 진행 상황 목록 조회")
  class FindRecipeProgressList {

    @Nested
    @DisplayName("Given - 유효한 레시피 ID가 주어졌을 때")
    class GivenValidRecipeId {

      private UUID recipeId;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
      }

      @Nested
      @DisplayName("When - 레시피 진행 상황 목록을 조회하면")
      class WhenFindRecipeProgressList {

        private List<RecipeProgress> recipeProgress;

        @BeforeEach
        void setUp() {
          recipeProgress =
              List.of(
                  RecipeProgress.create(
                      recipeId, clock, RecipeProgressStep.READY, RecipeProgressDetail.READY),
                  RecipeProgress.create(
                      recipeId, clock, RecipeProgressStep.STEP, RecipeProgressDetail.CAPTION));
          doReturn(recipeProgress)
              .when(recipeProgressRepository)
              .findAllByRecipeId(recipeId, RecipeProgressSort.CREATE_AT_ASC);
        }

        @DisplayName("Then - 해당 레시피의 진행 상황 목록이 반환된다")
        @Test
        void shouldReturnRecipeProgressList() {
          List<RecipeProgress> results = recipeProgressService.finds(recipeId);
          assert results.size() == 2;
          assert results.get(0).getStep() == RecipeProgressStep.READY;
          assert results.get(1).getDetail() == RecipeProgressDetail.CAPTION;
        }

        @DisplayName("Then - Repository에서 정렬 파라미터와 함께 호출된다")
        @Test
        void shouldCallRepositoryWithSortParameter() {
          recipeProgressService.finds(recipeId);
          verify(recipeProgressRepository)
              .findAllByRecipeId(recipeId, RecipeProgressSort.CREATE_AT_ASC);
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 진행 상황 정렬 조회")
  class FindRecipeProgressWithSort {

    @Nested
    @DisplayName("Given - 시간 순서가 다른 여러 레시피 진행 상황이 있을 때")
    class GivenMultipleRecipeProgressWithDifferentTime {

      private UUID recipeId;
      private List<RecipeProgress> sortedRecipeProgress;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();

        // 시간 순서대로 정렬된 RecipeProgress 목록 생성
        sortedRecipeProgress =
            List.of(
                RecipeProgress.create(
                    recipeId, clock, RecipeProgressStep.READY, RecipeProgressDetail.READY),
                RecipeProgress.create(
                    recipeId, clock, RecipeProgressStep.CAPTION, RecipeProgressDetail.CAPTION),
                RecipeProgress.create(
                    recipeId, clock, RecipeProgressStep.STEP, RecipeProgressDetail.STEP),
                RecipeProgress.create(
                    recipeId, clock, RecipeProgressStep.FINISHED, RecipeProgressDetail.FINISHED));

        doReturn(sortedRecipeProgress)
            .when(recipeProgressRepository)
            .findAllByRecipeId(recipeId, RecipeProgressSort.CREATE_AT_ASC);
      }

      @Nested
      @DisplayName("When - 레시피 진행 상황 목록을 조회하면")
      class WhenFindRecipeProgressList {

        @DisplayName("Then - createdAt 오름차순으로 정렬된 결과가 반환된다")
        @Test
        void shouldReturnRecipeProgressListSortedByCreatedAtAsc() {
          List<RecipeProgress> results = recipeProgressService.finds(recipeId);

          assert results.size() == 4;
          assert results.get(0).getStep() == RecipeProgressStep.READY;
          assert results.get(1).getStep() == RecipeProgressStep.CAPTION;
          assert results.get(2).getStep() == RecipeProgressStep.STEP;
          assert results.get(3).getStep() == RecipeProgressStep.FINISHED;

          // Repository 메서드가 올바른 정렬 파라미터와 함께 호출되는지 확인
          verify(recipeProgressRepository)
              .findAllByRecipeId(recipeId, RecipeProgressSort.CREATE_AT_ASC);
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 진행 상황 생성")
  class CreateRecipeProgress {

    @Nested
    @DisplayName("Given - 유효한 입력 값이 주어졌을 때")
    class GivenValidInputs {

      private UUID recipeId;
      private RecipeProgressStep step;
      private RecipeProgressDetail detail;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        step = RecipeProgressStep.STEP;
        detail = RecipeProgressDetail.STEP;
      }

      @Nested
      @DisplayName("When - 레시피 진행 상황을 생성하면")
      class WhenCreateRecipeProgress {

        @BeforeEach
        void setUp() {
          doReturn(RecipeProgress.create(recipeId, clock, step, detail))
              .when(recipeProgressRepository)
              .save(org.mockito.ArgumentMatchers.any(RecipeProgress.class));
        }

        @DisplayName("Then - 레시피 진행 상황이 성공적으로 생성된다")
        @Test
        void shouldCreateRecipeProgress() {
          recipeProgressService.create(recipeId, step, detail);
          verify(recipeProgressRepository)
              .save(org.mockito.ArgumentMatchers.any(RecipeProgress.class));
        }
      }
    }
  }
}
