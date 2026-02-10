package com.cheftory.api.recipe.creation.pipeline;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoErrorCode;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoException;
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

@DisplayName("RecipeCreationFinalizeStep 테스트")
class RecipeCreationFinalizeStepTest {

    private RecipeInfoService recipeInfoService;
    private RecipeProgressService recipeProgressService;
    private RecipeCreationFinalizeStep sut;

    @BeforeEach
    void setUp() {
        recipeInfoService = mock(RecipeInfoService.class);
        recipeProgressService = mock(RecipeProgressService.class);
        sut = createStep();
    }

    private RecipeCreationFinalizeStep createStep() {
        try {
            Constructor<RecipeCreationFinalizeStep> ctor = RecipeCreationFinalizeStep.class.getDeclaredConstructor(
                    RecipeInfoService.class, RecipeProgressService.class);
            ctor.setAccessible(true);
            return ctor.newInstance(recipeInfoService, recipeProgressService);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to create RecipeCreationFinalizeStep", ex);
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
                context = RecipeCreationExecutionContext.of(recipeId, "video-123", URI.create("https://youtu.be/video-123"), "test-title");
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
                            .start(recipeId, RecipeProgressStep.FINISHED, RecipeProgressDetail.FINISHED);
                }
            }
        }

        @Nested
        @DisplayName("Given - 파일 정보가 있고 정상 동작할 때")
        class GivenFileInfoAndSuccess {
            RecipeCreationExecutionContext context;
            UUID recipeId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                context = RecipeCreationExecutionContext.withFileInfo(
                        RecipeCreationExecutionContext.of(recipeId, "video-456", URI.create("https://youtu.be/video-456"), "test-title"),
                        "s3://bucket/file.mp4",
                        "video/mp4");
            }

            @Nested
            @DisplayName("When - 실행을 요청하면")
            class WhenRunning {

                @BeforeEach
                void setUp() throws RecipeException {
                    sut.run(context);
                }

                @Test
                @DisplayName("Then - 레시피를 완료 처리하고 진행 상태를 업데이트한다")
                void thenFinalizesRecipeAndUpdatesProgress() throws RecipeInfoException {
                    InOrder order = inOrder(recipeProgressService);
                    order.verify(recipeProgressService)
                            .start(recipeId, RecipeProgressStep.FINISHED, RecipeProgressDetail.FINISHED);
                    order.verify(recipeProgressService)
                            .success(recipeId, RecipeProgressStep.FINISHED, RecipeProgressDetail.FINISHED);

                    verify(recipeInfoService).success(recipeId);
                }
            }
        }

        @Nested
        @DisplayName("Given - 완료 처리 중 예외가 발생할 때")
        class GivenException {
            RecipeCreationExecutionContext context;
            UUID recipeId;

            @BeforeEach
            void setUp() throws RecipeInfoException {
                recipeId = UUID.randomUUID();
                context = RecipeCreationExecutionContext.withFileInfo(
                        RecipeCreationExecutionContext.of(recipeId, "video-789", URI.create("https://youtu.be/video-789"), "test-title"),
                        "s3://bucket/file.mp4",
                        "video/mp4");

                doThrow(new RecipeInfoException(RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND))
                        .when(recipeInfoService)
                        .success(recipeId);
            }

            @Nested
            @DisplayName("When - 실행을 요청하면")
            class WhenRunning {

                @Test
                @DisplayName("Then - 진행 상태를 실패로 기록하고 예외를 던진다")
                void thenFailsProgressAndThrowsException() {
                    assertThatThrownBy(() -> sut.run(context))
                            .isInstanceOf(RecipeException.class)
                            .hasFieldOrPropertyWithValue("error", RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND);

                    verify(recipeProgressService)
                            .failed(recipeId, RecipeProgressStep.FINISHED, RecipeProgressDetail.FINISHED);
                }
            }
        }
    }
}
