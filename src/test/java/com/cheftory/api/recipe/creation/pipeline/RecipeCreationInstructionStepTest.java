package com.cheftory.api.recipe.creation.pipeline;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cheftory.api.recipe.content.step.RecipeStepService;
import com.cheftory.api.recipe.creation.progress.RecipeProgressService;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

@DisplayName("RecipeCreationInstructionStep")
class RecipeCreationInstructionStepTest {

    private RecipeStepService recipeStepService;
    private RecipeProgressService recipeProgressService;
    private RecipeCreationInstructionStep sut;

    @BeforeEach
    void setUp() {
        recipeStepService = mock(RecipeStepService.class);
        recipeProgressService = mock(RecipeProgressService.class);
        sut = createStep();
    }

    private RecipeCreationInstructionStep createStep() {
        try {
            Constructor<RecipeCreationInstructionStep> ctor =
                    RecipeCreationInstructionStep.class.getDeclaredConstructor(
                            RecipeStepService.class, RecipeProgressService.class);
            ctor.setAccessible(true);
            return ctor.newInstance(recipeStepService, recipeProgressService);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to create RecipeCreationInstructionStep", ex);
        }
    }

    @Nested
    @DisplayName("run")
    class Run {

        @Test
        @DisplayName("file 정보가 없으면 RECIPE_CREATE_FAIL 예외가 발생한다")
        void shouldThrowWhenFileInfoMissing() {
            UUID recipeId = UUID.randomUUID();
            String videoId = "video-123";
            URI videoUrl = URI.create("https://youtu.be/video-123");
            RecipeCreationExecutionContext context = RecipeCreationExecutionContext.of(recipeId, videoId, videoUrl);

            assertThatThrownBy(() -> sut.run(context))
                    .isInstanceOf(RecipeException.class)
                    .hasFieldOrPropertyWithValue("error", RecipeErrorCode.RECIPE_CREATE_FAIL);

            verify(recipeProgressService, never()).start(recipeId, RecipeProgressStep.STEP, RecipeProgressDetail.STEP);
        }

        @Test
        @DisplayName("성공 시 step 생성과 progress가 기록된다")
        void shouldCreateStepsAndUpdateProgress() {
            UUID recipeId = UUID.randomUUID();
            String videoId = "video-456";
            URI videoUrl = URI.create("https://youtu.be/video-456");
            String fileUri = "s3://bucket/file.mp4";
            String mimeType = "video/mp4";
            RecipeCreationExecutionContext context = RecipeCreationExecutionContext.withFileInfo(
                    RecipeCreationExecutionContext.of(recipeId, videoId, videoUrl), fileUri, mimeType);

            sut.run(context);

            InOrder order = inOrder(recipeProgressService);
            order.verify(recipeProgressService).start(recipeId, RecipeProgressStep.STEP, RecipeProgressDetail.STEP);
            order.verify(recipeProgressService).success(recipeId, RecipeProgressStep.STEP, RecipeProgressDetail.STEP);

            verify(recipeStepService).create(recipeId, fileUri, mimeType);
        }

        @Test
        @DisplayName("예외 발생 시 progress를 failed로 기록한다")
        void shouldFailProgressWhenExceptionThrown() {
            UUID recipeId = UUID.randomUUID();
            String videoId = "video-789";
            URI videoUrl = URI.create("https://youtu.be/video-789");
            String fileUri = "s3://bucket/file.mp4";
            String mimeType = "video/mp4";
            RecipeCreationExecutionContext context = RecipeCreationExecutionContext.withFileInfo(
                    RecipeCreationExecutionContext.of(recipeId, videoId, videoUrl), fileUri, mimeType);

            when(recipeStepService.create(recipeId, fileUri, mimeType))
                    .thenThrow(new RecipeException(RecipeErrorCode.RECIPE_CREATE_FAIL));

            assertThatThrownBy(() -> sut.run(context))
                    .isInstanceOf(RecipeException.class)
                    .hasFieldOrPropertyWithValue("error", RecipeErrorCode.RECIPE_CREATE_FAIL);

            verify(recipeProgressService).failed(recipeId, RecipeProgressStep.STEP, RecipeProgressDetail.STEP);
        }
    }
}
