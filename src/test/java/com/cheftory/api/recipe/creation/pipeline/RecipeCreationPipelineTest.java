package com.cheftory.api.recipe.creation.pipeline;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cheftory.api.recipe.creation.progress.RecipeProgressService;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import com.cheftory.api.recipe.exception.RecipeException;
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

@DisplayName("RecipeCreationPipeline 테스트")
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
    @DisplayName("실행 (run)")
    class Run {

        @Nested
        @DisplayName("Given - 정상적인 파이프라인 실행 흐름일 때")
        class GivenNormalFlow {
            UUID recipeId;
            RecipeCreationExecutionContext context;
            RecipeCreationExecutionContext updatedContext;

            @BeforeEach
            void setUp() throws RecipeException {
                recipeId = UUID.randomUUID();
                context = RecipeCreationExecutionContext.of(recipeId, "video-123", URI.create("https://youtu.be/video-123"), null);
                updatedContext =
                        RecipeCreationExecutionContext.withFileInfo(context, "s3://bucket/file.mp4", "video/mp4");

                when(recipeCreationVerifyStep.run(context)).thenReturn(updatedContext);
            }

            @Nested
            @DisplayName("When - 실행을 요청하면")
            class WhenRunning {

                @BeforeEach
                void setUp() throws RecipeException {
                    sut.run(context);
                }

                @Test
                @DisplayName("Then - 단계별로 실행하고 마지막에 정리를 수행한다")
                void thenRunsStepsAndCleansUp() throws RecipeException {
                    InOrder order = inOrder(
                            recipeProgressService,
                            recipeCreationVerifyStep,
                            recipeCreationFinalizeStep,
                            recipeCreationCleanupStep);

                    order.verify(recipeProgressService)
                            .start(recipeId, RecipeProgressStep.READY, RecipeProgressDetail.READY);
                    order.verify(recipeCreationVerifyStep).run(context);
                    order.verify(recipeCreationFinalizeStep).run(updatedContext);
                    order.verify(recipeCreationCleanupStep).cleanup(updatedContext);
                }
            }
        }

        @Nested
        @DisplayName("Given - 파이프라인 실행 중 예외가 발생할 때")
        class GivenExceptionFlow {
            UUID recipeId;
            RecipeCreationExecutionContext context;
            RecipeCreationExecutionContext updatedContext;

            @BeforeEach
            void setUp() throws RecipeException {
                recipeId = UUID.randomUUID();
                context = RecipeCreationExecutionContext.of(recipeId, "video-456", URI.create("https://youtu.be/video-456"), null);
                updatedContext =
                        RecipeCreationExecutionContext.withFileInfo(context, "s3://bucket/file.mp4", "video/mp4");

                when(recipeCreationVerifyStep.run(context)).thenReturn(updatedContext);
                when(recipeCreationFinalizeStep.run(updatedContext)).thenThrow(new RuntimeException("fail"));
            }

            @Nested
            @DisplayName("When - 실행을 요청하면")
            class WhenRunning {

                @BeforeEach
                void setUp() {
                    try {
                        sut.run(context);
                    } catch (Exception ignored) {
                    }
                }

                @Test
                @DisplayName("Then - 예외가 발생해도 정리를 수행한다")
                void thenCleansUp() {
                    verify(recipeCreationCleanupStep).cleanup(updatedContext);
                }
            }
        }
    }
}
