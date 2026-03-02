package com.cheftory.api.recipe.creation.progress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgress;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressState;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeProgress 엔티티")
class RecipeProgressTest {

    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = mock(Clock.class);
        when(clock.now()).thenReturn(LocalDateTime.of(2026, 1, 1, 0, 0));
    }

    @Nested
    @DisplayName("생성 (create)")
    class Create {
        @Nested
        @DisplayName("Given - recipeId, jobId, 진행 정보가 주어졌을 때")
        class GivenValidParams {
            UUID recipeId;
            UUID jobId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                jobId = UUID.randomUUID();
            }

            @Nested
            @DisplayName("When - 생성하면")
            class WhenCreating {
                RecipeProgress progress;

                @BeforeEach
                void setUp() {
                    progress = RecipeProgress.create(
                            recipeId,
                            jobId,
                            clock,
                            RecipeProgressStep.READY,
                            RecipeProgressDetail.READY,
                            RecipeProgressState.RUNNING);
                }

                @Test
                @DisplayName("Then - recipeId/jobId를 포함한 진행 이벤트가 생성된다")
                void thenCreatesProgressEvent() {
                    assertThat(progress.getId()).isNotNull();
                    assertThat(progress.getRecipeId()).isEqualTo(recipeId);
                    assertThat(progress.getJobId()).isEqualTo(jobId);
                    assertThat(progress.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
                    assertThat(progress.getStep()).isEqualTo(RecipeProgressStep.READY);
                    assertThat(progress.getDetail()).isEqualTo(RecipeProgressDetail.READY);
                    assertThat(progress.getState()).isEqualTo(RecipeProgressState.RUNNING);
                }
            }
        }

        @Nested
        @DisplayName("Given - 다른 상태값으로 생성할 때")
        class GivenOtherStates {
            UUID recipeId;
            UUID jobId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                jobId = UUID.randomUUID();
            }

            @Test
            @DisplayName("Then - SUCCESS 상태 이벤트도 생성할 수 있다")
            void createsSuccessEvent() {
                RecipeProgress progress = RecipeProgress.create(
                        recipeId,
                        jobId,
                        clock,
                        RecipeProgressStep.FINISHED,
                        RecipeProgressDetail.FINISHED,
                        RecipeProgressState.SUCCESS);

                assertThat(progress.getState()).isEqualTo(RecipeProgressState.SUCCESS);
            }

            @Test
            @DisplayName("Then - FAILED 상태 이벤트도 생성할 수 있다")
            void createsFailedEvent() {
                RecipeProgress progress = RecipeProgress.create(
                        recipeId,
                        jobId,
                        clock,
                        RecipeProgressStep.FINISHED,
                        RecipeProgressDetail.FINISHED,
                        RecipeProgressState.FAILED);

                assertThat(progress.getState()).isEqualTo(RecipeProgressState.FAILED);
            }
        }
    }
}
