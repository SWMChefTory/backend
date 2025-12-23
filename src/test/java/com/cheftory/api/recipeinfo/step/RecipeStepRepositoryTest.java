package com.cheftory.api.recipeinfo.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import com.cheftory.api.DbContextTest;
import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.step.entity.RecipeStep;
import com.cheftory.api.recipeinfo.step.entity.RecipeStepSort;
import com.cheftory.api.recipeinfo.step.repository.RecipeStepRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DisplayName("RecipeStepRepository")
public class RecipeStepRepositoryTest extends DbContextTest {

  @Autowired private RecipeStepRepository recipeStepRepository;

  @MockitoBean private Clock clock;

  @Nested
  @DisplayName("레시피 단계 저장")
  class SaveRecipeStep {

    @Nested
    @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
    class GivenValidParameters {

      private String subtitle;
      private Double start;
      private String descriptionText;
      private Double descriptionStart;
      private UUID recipeId;
      private final LocalDateTime FIXED_TIME = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

      @BeforeEach
      void setUp() {
        subtitle = "Step 1";
        start = 0.0;
        descriptionText = "First, do this.";
        descriptionStart = 0.0;
        recipeId = UUID.randomUUID();
        doReturn(FIXED_TIME).when(clock).now();
      }

      @Nested
      @DisplayName("When - 레시피 단계를 저장하면")
      class WhenSaveRecipeStep {

        private RecipeStep recipeStep;
        private RecipeStep.Detail recipeStepDetail;

        @BeforeEach
        void setUp() {
          recipeStepDetail = RecipeStep.Detail.of(descriptionText, descriptionStart);

          recipeStep =
              RecipeStep.create(1, subtitle, List.of(recipeStepDetail), start, recipeId, clock);

          recipeStepRepository.save(recipeStep);
        }

        @DisplayName("Then - 레시피 단계가 저장된다")
        @Test
        void thenRecipeStepIsSaved() {
          List<RecipeStep> recipeSteps = recipeStepRepository.findAllByRecipeId(recipeId, null);
          assertThat(recipeSteps).hasSize(1);

          RecipeStep savedStep = recipeSteps.getFirst();
          assertThat(savedStep.getId()).isEqualTo(recipeStep.getId());
          assertThat(savedStep.getStepOrder()).isEqualTo(1);
          assertThat(savedStep.getSubtitle()).isEqualTo(subtitle);
          assertThat(savedStep.getStart()).isEqualTo(start);
          assertThat(savedStep.getRecipeId()).isEqualTo(recipeId);
          assertThat(savedStep.getCreatedAt()).isEqualTo(FIXED_TIME);

          assertThat(savedStep.getDetails()).hasSize(1);
          RecipeStep.Detail savedDetail = savedStep.getDetails().getFirst();
          assertThat(savedDetail.getText()).isEqualTo(descriptionText);
          assertThat(savedDetail.getStart()).isEqualTo(descriptionStart);
        }

        @DisplayName("Then - ID로 개별 조회가 가능하다")
        @Test
        void thenCanFindById() {
          Optional<RecipeStep> foundStep = recipeStepRepository.findById(recipeStep.getId());

          assertThat(foundStep).isPresent();
          assertThat(foundStep.get().getId()).isEqualTo(recipeStep.getId());
          assertThat(foundStep.get().getSubtitle()).isEqualTo(subtitle);
        }
      }
    }

    @Nested
    @DisplayName("Given - 여러 개의 Detail을 가진 레시피 단계가 주어졌을 때")
    class GivenMultipleDetails {

      private UUID recipeId;
      private LocalDateTime fixedNow;

      @BeforeEach
      void setUp() {
        fixedNow = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        recipeId = UUID.randomUUID();
        doReturn(fixedNow).when(clock).now();
      }

      @DisplayName("When - 복수의 Detail을 가진 단계를 저장하면")
      @Nested
      class WhenSaveStepWithMultipleDetails {

        private RecipeStep recipeStep;

        @BeforeEach
        void setUp() {
          List<RecipeStep.Detail> details =
              List.of(
                  RecipeStep.Detail.of("첫 번째 동작", 0.0),
                  RecipeStep.Detail.of("두 번째 동작", 30.5),
                  RecipeStep.Detail.of("세 번째 동작", 60.0));

          recipeStep = RecipeStep.create(1, "복합 단계", details, 0.0, recipeId, clock);

          recipeStepRepository.save(recipeStep);
        }

        @DisplayName("Then - 모든 Detail이 정상적으로 저장된다")
        @Test
        void thenAllDetailsAreSaved() {
          RecipeStep savedStep = recipeStepRepository.findById(recipeStep.getId()).orElseThrow();

          assertThat(savedStep.getDetails()).hasSize(3);
          assertThat(savedStep.getDetails())
              .extracting(RecipeStep.Detail::getText)
              .containsExactly("첫 번째 동작", "두 번째 동작", "세 번째 동작");
          assertThat(savedStep.getDetails())
              .extracting(RecipeStep.Detail::getStart)
              .containsExactly(0.0, 30.5, 60.0);
        }
      }
    }
  }

  @Nested
  @DisplayName("레시피 단계 조회")
  class FindRecipeSteps {

    @DisplayName("Given - 여러 레시피 단계가 저장되어 있을 때")
    @Nested
    class GivenMultipleRecipeSteps {

      private UUID recipeId;
      private LocalDateTime fixedNow;
      private LocalDateTime fixedLater;
      private RecipeStep step1;
      private RecipeStep step2;

      @BeforeEach
      void setUp() {
        fixedNow = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        fixedLater = LocalDateTime.of(2024, 1, 1, 13, 0, 0);
        recipeId = UUID.randomUUID();

        doReturn(fixedLater).when(clock).now();
        step1 =
            RecipeStep.create(
                1,
                "Step 1",
                List.of(RecipeStep.Detail.of("첫 번째 단계", 0.0)),
                0.0,
                recipeId,
                clock);

        doReturn(fixedNow).when(clock).now();
        step2 =
            RecipeStep.create(
                2,
                "Step 2",
                List.of(RecipeStep.Detail.of("두 번째 단계", 30.0)),
                30.0,
                recipeId,
                clock);

        recipeStepRepository.save(step2);
        recipeStepRepository.save(step1);
      }

      @Nested
      @DisplayName("When - 특정 레시피 ID로 단계들을 조회하면")
      class WhenFindRecipeStepsByRecipeId {

        @DisplayName("Then - 해당 레시피의 단계들만 반환된다")
        @Test
        void thenOnlyTargetRecipeStepsAreReturned() {
          List<RecipeStep> recipeSteps = recipeStepRepository.findAllByRecipeId(recipeId, null);

          assertThat(recipeSteps).hasSize(2);
          assertThat(recipeSteps).extracting(RecipeStep::getRecipeId).containsOnly(recipeId);
          assertThat(recipeSteps)
              .extracting(RecipeStep::getSubtitle)
              .containsExactlyInAnyOrder("Step 1", "Step 2");
        }

        @DisplayName("Then - stepOrder로 정렬된다")
        @Test
        void thenStepsAreOrderedByStepOrder() {
          List<RecipeStep> recipeSteps =
              recipeStepRepository.findAllByRecipeId(recipeId, RecipeStepSort.STEP_ORDER_ASC);

          assertThat(recipeSteps).extracting(RecipeStep::getStepOrder).containsExactly(1, 2);
        }
      }

      @DisplayName("When - 존재하지 않는 레시피 ID로 조회하면")
      @Nested
      class WhenFindWithNonExistentRecipeId {

        @DisplayName("Then - 빈 리스트가 반환된다")
        @Test
        void thenEmptyListIsReturned() {
          UUID nonExistentRecipeId = UUID.randomUUID();
          List<RecipeStep> recipeSteps =
              recipeStepRepository.findAllByRecipeId(nonExistentRecipeId, null);

          assertThat(recipeSteps).isEmpty();
        }
      }
    }
  }
}
