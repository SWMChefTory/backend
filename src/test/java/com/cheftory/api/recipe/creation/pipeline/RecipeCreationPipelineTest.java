package com.cheftory.api.recipe.creation.pipeline;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cheftory.api.recipe.creation.progress.RecipeProgressService;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import java.lang.reflect.Constructor;
import java.net.URI;
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
    private RecipeCreationVerifyStep recipeCreationVerifyStep;
    private RecipeCreationDetailStep recipeCreationDetailStep;
    private RecipeCreationInstructionStep recipeCreationInstructionStep;
    private RecipeCreationBriefingStep recipeCreationBriefingStep;
    private RecipeCreationFinalizeStep recipeCreationFinalizeStep;
    private RecipeCreationCleanupStep recipeCreationCleanupStep;
    private AsyncTaskExecutor recipeCreateExecutor;

    private RecipeCreationPipeline sut;

    @BeforeEach
    void setUp() {
        recipeProgressService = mock(RecipeProgressService.class);
        recipeCreationVerifyStep = mock(RecipeCreationVerifyStep.class);
        recipeCreationDetailStep = mock(RecipeCreationDetailStep.class);
        recipeCreationInstructionStep = mock(RecipeCreationInstructionStep.class);
        recipeCreationBriefingStep = mock(RecipeCreationBriefingStep.class);
        recipeCreationFinalizeStep = mock(RecipeCreationFinalizeStep.class);
        recipeCreationCleanupStep = mock(RecipeCreationCleanupStep.class);
        recipeCreateExecutor = mock(AsyncTaskExecutor.class);

        doAnswer(invocation -> {
                    Runnable task = invocation.getArgument(0);
                    task.run();
                    return null;
                })
                .when(recipeCreateExecutor)
                .execute(ArgumentMatchers.any(Runnable.class));

        sut = createPipeline();
    }

    private RecipeCreationPipeline createPipeline() {
        try {
            Constructor<RecipeCreationPipeline> ctor = RecipeCreationPipeline.class.getDeclaredConstructor(
                    RecipeProgressService.class,
                    RecipeCreationVerifyStep.class,
                    RecipeCreationDetailStep.class,
                    RecipeCreationInstructionStep.class,
                    RecipeCreationBriefingStep.class,
                    RecipeCreationFinalizeStep.class,
                    RecipeCreationCleanupStep.class,
                    AsyncTaskExecutor.class);
            ctor.setAccessible(true);
            return ctor.newInstance(
                    recipeProgressService,
                    recipeCreationVerifyStep,
                    recipeCreationDetailStep,
                    recipeCreationInstructionStep,
                    recipeCreationBriefingStep,
                    recipeCreationFinalizeStep,
                    recipeCreationCleanupStep,
                    recipeCreateExecutor);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to create RecipeCreationPipeline", ex);
        }
    }

    @Nested
    @DisplayName("run")
    class Run {

        @Test
        @DisplayName("파이프라인 단계가 순서대로 실행되고 마지막에 cleanup이 호출된다")
        void shouldRunPipelineInOrderAndCleanup() {
            UUID recipeId = UUID.randomUUID();
            String videoId = "video-123";
            URI videoUrl = URI.create("https://youtu.be/video-123");
            RecipeCreationExecutionContext context = RecipeCreationExecutionContext.of(recipeId, videoId, videoUrl);
            RecipeCreationExecutionContext updatedContext = RecipeCreationExecutionContext.withFileInfo(
                    context, "s3://bucket/file.mp4", "video/mp4");

            when(recipeCreationVerifyStep.run(context)).thenReturn(updatedContext);

            sut.run(context);

            InOrder order = inOrder(recipeProgressService, recipeCreationVerifyStep, recipeCreationFinalizeStep, recipeCreationCleanupStep);

            order.verify(recipeProgressService).start(recipeId, RecipeProgressStep.READY, RecipeProgressDetail.READY);
            order.verify(recipeCreationVerifyStep).run(context);
            order.verify(recipeCreationFinalizeStep).run(updatedContext);
            order.verify(recipeCreationCleanupStep).cleanup(updatedContext);
        }

        @Test
        @DisplayName("파이프라인 도중 예외가 발생해도 cleanup이 호출된다")
        void shouldCleanupEvenWhenExceptionThrown() {
            UUID recipeId = UUID.randomUUID();
            String videoId = "video-456";
            URI videoUrl = URI.create("https://youtu.be/video-456");
            RecipeCreationExecutionContext context = RecipeCreationExecutionContext.of(recipeId, videoId, videoUrl);
            RecipeCreationExecutionContext updatedContext = RecipeCreationExecutionContext.withFileInfo(
                    context, "s3://bucket/file.mp4", "video/mp4");

            when(recipeCreationVerifyStep.run(context)).thenReturn(updatedContext);
            when(recipeCreationFinalizeStep.run(updatedContext)).thenThrow(new RuntimeException("fail"));

            try {
                sut.run(context);
            } catch (Exception ignored) {}

            verify(recipeCreationCleanupStep).cleanup(updatedContext);
        }
    }
}
