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
          doReturn(recipeProgress).when(recipeProgressRepository).findAllByRecipeId(recipeId);
        }

        @DisplayName("Then - 해당 레시피의 진행 상황 목록이 반환된다")
        @Test
        void shouldReturnRecipeProgressList() {
          List<RecipeProgress> results = recipeProgressService.finds(recipeId);
          assert results.size() == 2;
          assert results.get(0).getStep() == RecipeProgressStep.READY;
          assert results.get(1).getDetail() == RecipeProgressDetail.CAPTION;
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
