package com.cheftory.api.recipe.creation.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RecipeCreationParallelSteps")
class RecipeCreationParallelStepsTest {

    @Test
    @DisplayName("모든 step이 executor를 통해 실행된다")
    void shouldRunAllSteps() {
        Executor directExecutor = Runnable::run;
        RecipeCreationPipelineStep step1 = mock(RecipeCreationPipelineStep.class);
        RecipeCreationPipelineStep step2 = mock(RecipeCreationPipelineStep.class);
        RecipeCreationPipelineStep step3 = mock(RecipeCreationPipelineStep.class);

        RecipeCreationParallelSteps sut = new RecipeCreationParallelSteps(directExecutor, List.of(step1, step2, step3));

        RecipeCreationExecutionContext context = RecipeCreationExecutionContext.of(
                UUID.randomUUID(), "video-123", URI.create("https://youtu.be/video-123"));

        RecipeCreationExecutionContext result = sut.run(context);

        verify(step1).run(context);
        verify(step2).run(context);
        verify(step3).run(context);
        assertThat(result).isSameAs(context);
    }
}
