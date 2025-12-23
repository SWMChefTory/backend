package com.cheftory.api.recipeinfo.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.step.entity.RecipeStep;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeStepTest")
public class RecipeStepTest {

  @Nested
  @DisplayName("레시피 단계 생성")
  class CreateRecipeStep {

    @Nested
    @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
    class GivenValidParameters {

      private Integer stepOrder;
      private String subtitle;
      private Double start;
      private UUID recipeId;
      private String detailText1;
      private Double detailStart1;
      private String detailText2;
      private Double detailStart2;

      @BeforeEach
      void setUp() {
        stepOrder = 1;
        subtitle = "Step 1";
        start = 0.0;
        recipeId = UUID.randomUUID();
        detailText1 = "First, do this.";
        detailStart1 = 0.0;
        detailText2 = "Then, do that.";
        detailStart2 = 5.0;
      }

      @Nested
      @DisplayName("When - 레시피 단계를 생성하면")
      class WhenCreateRecipeStep {

        private RecipeStep.Detail recipeStepDetail1;
        private RecipeStep.Detail recipeStepDetail2;
        private Clock clock;

        @BeforeEach
        void setUp() {
          clock = mock(Clock.class);
          recipeStepDetail1 = mock(RecipeStep.Detail.class);
          recipeStepDetail2 = mock(RecipeStep.Detail.class);
          doReturn(LocalDateTime.now()).when(clock).now();
          doReturn(detailStart1).when(recipeStepDetail1).getStart();
          doReturn(detailText1).when(recipeStepDetail1).getText();
          doReturn(detailStart2).when(recipeStepDetail2).getStart();
          doReturn(detailText2).when(recipeStepDetail2).getText();
        }

        @Nested
        @DisplayName("Then - 레시피 단계가 생성된다")
        class ThenRecipeStepCreated {

          private RecipeStep recipeStep;

          @BeforeEach
          void setUp() {
            recipeStep =
                RecipeStep.create(
                    stepOrder,
                    subtitle,
                    List.of(recipeStepDetail1, recipeStepDetail2),
                    start,
                    recipeId,
                    clock);
          }

          @DisplayName("레시피 단계가 올바르게 생성되었는지 확인한다")
          @Test
          void itCreatesRecipeStep() {
            assertThat(recipeStep).isNotNull();
            assertThat(recipeStep.getId()).isNotNull();
            assertThat(recipeStep.getStepOrder()).isEqualTo(stepOrder);
            assertThat(recipeStep.getSubtitle()).isEqualTo(subtitle);
            assertThat(recipeStep.getStart()).isEqualTo(start);
            assertThat(recipeStep.getRecipeId()).isEqualTo(recipeId);
            assertThat(recipeStep.getCreatedAt()).isNotNull();
            assertThat(recipeStep.getDetails()).hasSize(2);
            assertThat(recipeStep.getDetails().get(0).getText()).isEqualTo(detailText1);
            assertThat(recipeStep.getDetails().get(0).getStart()).isEqualTo(detailStart1);
            assertThat(recipeStep.getDetails().get(1).getText()).isEqualTo(detailText2);
            assertThat(recipeStep.getDetails().get(1).getStart()).isEqualTo(detailStart2);
          }
        }
      }

      @Nested
      @DisplayName("When - 레시피 단계 상세를 생성하면")
      class WhenCreateRecipeStepDetail {
        private RecipeStep.Detail recipeStepDetail;

        @BeforeEach
        void setUp() {
          recipeStepDetail =
              RecipeStep.Detail.of(detailText1, detailStart1);
        }

        @Nested
        @DisplayName("Then - 레시피 단계 상세가 생성된다")
        class ThenRecipeStepDetailCreated {

          @DisplayName("레시피 단계 상세가 올바르게 생성되었는지 확인한다")
          @Test
          void itCreatesRecipeStepDetail() {
            assertThat(recipeStepDetail).isNotNull();
            assertThat(recipeStepDetail.getText()).isEqualTo(detailText1);
            assertThat(recipeStepDetail.getStart()).isEqualTo(detailStart1);
          }
        }
      }
    }
  }
}
