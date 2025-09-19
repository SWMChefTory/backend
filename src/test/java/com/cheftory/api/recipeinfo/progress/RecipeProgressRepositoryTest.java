package com.cheftory.api.recipeinfo.progress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
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

  private LocalDateTime now;

  @BeforeEach
  void setUp() {
    now = LocalDateTime.now();
    doReturn(now).when(clock).now();
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
          results = recipeProgressRepository.findAllByRecipeId(recipeId);
        }

        @DisplayName("Then - 해당 레시피의 진행 상황이 반환된다")
        @Test
        void shouldReturnRecipeProgress() {

          assertThat(results).hasSize(1);
          assertThat(results.getFirst().getRecipeId()).isEqualTo(recipeId);
          assertThat(results.getFirst().getStep()).isEqualTo(step);
          assertThat(results.getFirst().getDetail()).isEqualTo(detail);
          assertThat(results.getFirst().getCreatedAt()).isEqualTo(now);
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
          List<RecipeProgress> results = recipeProgressRepository.findAllByRecipeId(recipeId);

          assertThat(results).hasSize(1);
          assertThat(results.getFirst().getRecipeId()).isEqualTo(recipeId);
          assertThat(results.getFirst().getStep()).isEqualTo(step);
          assertThat(results.getFirst().getDetail()).isEqualTo(detail);
          assertThat(results.getFirst().getCreatedAt()).isEqualTo(now);
        }
      }
    }
  }
}
