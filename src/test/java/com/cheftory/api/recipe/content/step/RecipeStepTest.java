package com.cheftory.api.recipe.content.step;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.step.entity.RecipeStep;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeStep 엔티티")
public class RecipeStepTest {

    @Nested
    @DisplayName("레시피 단계 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 파라미터가 주어졌을 때")
        class GivenValidParameters {
            Integer stepOrder;
            String subtitle;
            Double start;
            UUID recipeId;
            String detailText1;
            Double detailStart1;
            String detailText2;
            Double detailStart2;
            Clock clock;
            RecipeStep.Detail recipeStepDetail1;
            RecipeStep.Detail recipeStepDetail2;

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
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {
                RecipeStep recipeStep;

                @BeforeEach
                void setUp() {
                    recipeStep = RecipeStep.create(
                            stepOrder, subtitle, List.of(recipeStepDetail1, recipeStepDetail2), start, recipeId, clock);
                }

                @Test
                @DisplayName("Then - 레시피 단계가 올바르게 생성된다")
                void thenCreatedCorrectly() {
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
    }

    @Nested
    @DisplayName("레시피 단계 상세 생성 (Detail.of)")
    class CreateDetail {

        @Nested
        @DisplayName("Given - 텍스트와 시작 시간이 주어졌을 때")
        class GivenTextAndStart {
            String text;
            Double start;

            @BeforeEach
            void setUp() {
                text = "Detail text";
                start = 10.5;
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {
                RecipeStep.Detail detail;

                @BeforeEach
                void setUp() {
                    detail = RecipeStep.Detail.of(text, start);
                }

                @Test
                @DisplayName("Then - 상세 정보가 올바르게 생성된다")
                void thenCreatedCorrectly() {
                    assertThat(detail).isNotNull();
                    assertThat(detail.getText()).isEqualTo(text);
                    assertThat(detail.getStart()).isEqualTo(start);
                }
            }
        }
    }
}
