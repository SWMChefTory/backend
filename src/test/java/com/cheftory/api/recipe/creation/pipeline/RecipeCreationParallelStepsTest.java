package com.cheftory.api.recipe.creation.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cheftory.api.recipe.exception.RecipeException;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeCreationParallelSteps 테스트")
class RecipeCreationParallelStepsTest {

    @Nested
    @DisplayName("실행 (run)")
    class Run {

        @Nested
        @DisplayName("Given - 여러 단계가 주어졌을 때")
        class GivenMultipleSteps {
            Executor directExecutor;
            RecipeCreationPipelineStep step1;
            RecipeCreationPipelineStep step2;
            RecipeCreationPipelineStep step3;
            RecipeCreationParallelSteps sut;
            RecipeCreationExecutionContext context;

            @BeforeEach
            void setUp() {
                directExecutor = Runnable::run;
                step1 = mock(RecipeCreationPipelineStep.class);
                step2 = mock(RecipeCreationPipelineStep.class);
                step3 = mock(RecipeCreationPipelineStep.class);
                sut = new RecipeCreationParallelSteps(directExecutor, List.of(step1, step2, step3));
                context = RecipeCreationExecutionContext.of(
                        UUID.randomUUID(), "video-123", URI.create("https://youtu.be/video-123"), null);
            }

            @Nested
            @DisplayName("When - 실행을 요청하면")
            class WhenRunning {

                @BeforeEach
                void setUp() throws RecipeException {
                    sut.run(context);
                }

                @Test
                @DisplayName("Then - 모든 단계를 병렬로 실행하고 컨텍스트를 반환한다")
                void thenRunsAllSteps() throws RecipeException {
                    verify(step1).run(context);
                    verify(step2).run(context);
                    verify(step3).run(context);
                    assertThat(sut.run(context)).isSameAs(context);
                }
            }
        }
    }
}
