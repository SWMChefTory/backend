package com.cheftory.api.recipe.creation.pipeline;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cheftory.api.recipe.content.step.RecipeStepService;
import com.cheftory.api.recipe.content.step.exception.RecipeStepErrorCode;
import com.cheftory.api.recipe.content.step.exception.RecipeStepException;
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

@DisplayName("RecipeCreationInstructionStep 테스트")
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
    @DisplayName("실행 (run)")
    class Run {

        @Nested
        @DisplayName("Given - 파일 정보가 없을 때")
        class GivenNoFileInfo {
            RecipeCreationExecutionContext context;
            UUID recipeId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                context = RecipeCreationExecutionContext.of(
                        recipeId, "video-123", URI.create("https://youtu.be/video-123"), "test-title");
            }

            @Nested
            @DisplayName("When - 실행을 요청하면")
            class WhenRunning {

                @Test
                @DisplayName("Then - RECIPE_CREATE_FAIL 예외를 던진다")
                void thenThrowsException() {
                    assertThatThrownBy(() -> sut.run(context))
                            .isInstanceOf(RecipeException.class)
                            .hasFieldOrPropertyWithValue("error", RecipeErrorCode.RECIPE_CREATE_FAIL);

                    verify(recipeProgressService, never())
                            .start(recipeId, RecipeProgressStep.STEP, RecipeProgressDetail.STEP);
                }
            }
        }

        @Nested
        @DisplayName("Given - 파일 정보가 있고 정상 동작할 때")
        class GivenFileInfoAndSuccess {
            RecipeCreationExecutionContext context;
            UUID recipeId;
            String fileUri;
            String mimeType;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                fileUri = "s3://bucket/file.mp4";
                mimeType = "video/mp4";
                context = RecipeCreationExecutionContext.withFileInfo(
                        RecipeCreationExecutionContext.of(
                                recipeId, "video-456", URI.create("https://youtu.be/video-456"), "test-title"),
                        fileUri,
                        mimeType);
            }

            @Nested
            @DisplayName("When - 실행을 요청하면")
            class WhenRunning {

                @BeforeEach
                void setUp() throws RecipeException {
                    sut.run(context);
                }

                @Test
                @DisplayName("Then - 단계를 생성하고 진행 상태를 업데이트한다")
                void thenCreatesStepsAndUpdatesProgress() throws RecipeStepException {
                    InOrder order = inOrder(recipeProgressService);
                    order.verify(recipeProgressService)
                            .start(recipeId, RecipeProgressStep.STEP, RecipeProgressDetail.STEP);
                    order.verify(recipeProgressService)
                            .success(recipeId, RecipeProgressStep.STEP, RecipeProgressDetail.STEP);

                    verify(recipeStepService).create(recipeId, fileUri, mimeType);
                }
            }
        }

        @Nested
        @DisplayName("Given - 단계 생성 중 예외가 발생할 때")
        class GivenException {
            RecipeCreationExecutionContext context;
            UUID recipeId;
            String fileUri;
            String mimeType;

            @BeforeEach
            void setUp() throws RecipeStepException {
                recipeId = UUID.randomUUID();
                fileUri = "s3://bucket/file.mp4";
                mimeType = "video/mp4";
                context = RecipeCreationExecutionContext.withFileInfo(
                        RecipeCreationExecutionContext.of(
                                recipeId, "video-789", URI.create("https://youtu.be/video-789"), "test-title"),
                        fileUri,
                        mimeType);

                when(recipeStepService.create(recipeId, fileUri, mimeType))
                        .thenThrow(new RecipeStepException(RecipeStepErrorCode.RECIPE_STEP_CREATE_FAIL));
            }

            @Nested
            @DisplayName("When - 실행을 요청하면")
            class WhenRunning {

                @Test
                @DisplayName("Then - 진행 상태를 실패로 기록하고 예외를 던진다")
                void thenFailsProgressAndThrowsException() {
                    assertThatThrownBy(() -> sut.run(context))
                            .isInstanceOf(RecipeStepException.class)
                            .hasFieldOrPropertyWithValue("error", RecipeStepErrorCode.RECIPE_STEP_CREATE_FAIL);

                    verify(recipeProgressService).failed(recipeId, RecipeProgressStep.STEP, RecipeProgressDetail.STEP);
                }
            }
        }
    }
}
