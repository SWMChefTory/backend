package com.cheftory.api.recipe.creation.pipeline;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cheftory.api.recipe.creation.progress.RecipeProgressService;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import java.net.URI;
import java.lang.reflect.Constructor;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.springframework.core.task.AsyncTaskExecutor;

@DisplayName("RecipeCreationPipeline")
class RecipeCreationPipelineTest {

    private RecipeProgressService recipeProgressService;
    private RecipeCreationCaptionStep recipeCreationCaptionStep;
    private RecipeCreationDetailStep recipeCreationDetailStep;
    private RecipeCreationInstructionStep recipeCreationInstructionStep;
    private RecipeCreationBriefingStep recipeCreationBriefingStep;
    private RecipeCreationFinalizeStep recipeCreationFinalizeStep;
    private AsyncTaskExecutor recipeCreateExecutor;

    private RecipeCreationPipeline sut;

    @BeforeEach
    void setUp() {
        recipeProgressService = mock(RecipeProgressService.class);
        recipeCreationCaptionStep = mock(RecipeCreationCaptionStep.class);
        recipeCreationDetailStep = mock(RecipeCreationDetailStep.class);
        recipeCreationInstructionStep = mock(RecipeCreationInstructionStep.class);
        recipeCreationBriefingStep = mock(RecipeCreationBriefingStep.class);
        recipeCreationFinalizeStep = mock(RecipeCreationFinalizeStep.class);
        recipeCreateExecutor = mock(AsyncTaskExecutor.class);

        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(recipeCreateExecutor).execute(ArgumentMatchers.any(Runnable.class));

        sut = createPipeline();
    }

    private RecipeCreationPipeline createPipeline() {
        try {
            Constructor<RecipeCreationPipeline> ctor = RecipeCreationPipeline.class.getDeclaredConstructor(
                    RecipeProgressService.class,
                    RecipeCreationCaptionStep.class,
                    RecipeCreationDetailStep.class,
                    RecipeCreationInstructionStep.class,
                    RecipeCreationBriefingStep.class,
                    RecipeCreationFinalizeStep.class,
                    AsyncTaskExecutor.class);
            ctor.setAccessible(true);
            return ctor.newInstance(
                    recipeProgressService,
                    recipeCreationCaptionStep,
                    recipeCreationDetailStep,
                    recipeCreationInstructionStep,
                    recipeCreationBriefingStep,
                    recipeCreationFinalizeStep,
                    recipeCreateExecutor);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to create RecipeCreationPipeline", ex);
        }
    }

    @Nested
    @DisplayName("run")
    class Run {

        @Test
        @DisplayName("파이프라인 단계가 순서대로 실행된다")
        void shouldRunPipelineInOrder() {
            UUID recipeId = UUID.randomUUID();
            String videoId = "video-123";
            URI videoUrl = URI.create("https://youtu.be/video-123");
            RecipeCreationExecutionContext context =
                    RecipeCreationExecutionContext.of(recipeId, videoId, videoUrl);
            RecipeCreationExecutionContext updatedContext =
                    RecipeCreationExecutionContext.from(
                            context, mock(com.cheftory.api.recipe.content.caption.entity.RecipeCaption.class));

            when(recipeCreationCaptionStep.run(context)).thenReturn(updatedContext);

            sut.run(context);

            InOrder order = inOrder(
                    recipeProgressService,
                    recipeCreationCaptionStep,
                    recipeCreationFinalizeStep);

            order.verify(recipeProgressService)
                    .start(recipeId, RecipeProgressStep.READY, RecipeProgressDetail.READY);
            order.verify(recipeCreationCaptionStep).run(context);

            verify(recipeCreationFinalizeStep).run(updatedContext);
        }

        @Test
        @DisplayName("병렬 단계 실행은 새로운 RecipeCreationParallelSteps로 위임된다")
        void shouldDelegateParallelStepsExecution() {
            UUID recipeId = UUID.randomUUID();
            String videoId = "video-456";
            URI videoUrl = URI.create("https://youtu.be/video-456");
            RecipeCreationExecutionContext context =
                    RecipeCreationExecutionContext.of(recipeId, videoId, videoUrl);
            RecipeCreationExecutionContext updatedContext =
                    RecipeCreationExecutionContext.from(
                            context, mock(com.cheftory.api.recipe.content.caption.entity.RecipeCaption.class));

            when(recipeCreationCaptionStep.run(context)).thenReturn(updatedContext);

            sut.run(context);

            verify(recipeCreationDetailStep).run(updatedContext);
            verify(recipeCreationInstructionStep).run(updatedContext);
            verify(recipeCreationBriefingStep).run(updatedContext);
            verify(recipeCreationFinalizeStep).run(updatedContext);
        }
    }
}
