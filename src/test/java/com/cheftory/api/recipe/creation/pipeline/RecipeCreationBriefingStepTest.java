package com.cheftory.api.recipe.creation.pipeline;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.cheftory.api.recipe.content.briefing.RecipeBriefingService;
import com.cheftory.api.recipe.content.briefing.exception.RecipeBriefingErrorCode;
import com.cheftory.api.recipe.content.briefing.exception.RecipeBriefingException;
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

@DisplayName("RecipeCreationBriefingStep 테스트")
class RecipeCreationBriefingStepTest {

    private RecipeBriefingService recipeBriefingService;
    private RecipeProgressService recipeProgressService;
    private RecipeCreationBriefingStep sut;

    @BeforeEach
    void setUp() {
        recipeBriefingService = mock(RecipeBriefingService.class);
        recipeProgressService = mock(RecipeProgressService.class);
        sut = createStep();
    }

    private RecipeCreationBriefingStep createStep() {
        try {
            Constructor<RecipeCreationBriefingStep> ctor = RecipeCreationBriefingStep.class.getDeclaredConstructor(
                    RecipeBriefingService.class, RecipeProgressService.class);
            ctor.setAccessible(true);
            return ctor.newInstance(recipeBriefingService, recipeProgressService);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to create RecipeCreationBriefingStep", ex);
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
                            .start(recipeId, RecipeProgressStep.BRIEFING, RecipeProgressDetail.BRIEFING);
                }
            }
        }

        @Nested
        @DisplayName("Given - 파일 정보가 있고 정상 동작할 때")
        class GivenFileInfoAndSuccess {
            RecipeCreationExecutionContext context;
            UUID recipeId;
            String videoId;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                videoId = "video-456";
                context = RecipeCreationExecutionContext.withFileInfo(
                        RecipeCreationExecutionContext.of(
                                recipeId, videoId, URI.create("https://youtu.be/video-456"), "test-title"),
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
                @DisplayName("Then - 브리핑을 생성하고 진행 상태를 업데이트한다")
                void thenCreatesBriefingAndUpdatesProgress() throws RecipeBriefingException {
                    InOrder order = inOrder(recipeProgressService);
                    order.verify(recipeProgressService)
                            .start(recipeId, RecipeProgressStep.BRIEFING, RecipeProgressDetail.BRIEFING);
                    order.verify(recipeProgressService)
                            .success(recipeId, RecipeProgressStep.BRIEFING, RecipeProgressDetail.BRIEFING);

                    verify(recipeBriefingService).create(videoId, recipeId);
                }
            }
        }

        @Nested
        @DisplayName("Given - 브리핑 생성 중 예외가 발생할 때")
        class GivenException {
            RecipeCreationExecutionContext context;
            UUID recipeId;

            @BeforeEach
            void setUp() throws RecipeBriefingException {
                recipeId = UUID.randomUUID();
                context = RecipeCreationExecutionContext.withFileInfo(
                        RecipeCreationExecutionContext.of(
                                recipeId, "video-789", URI.create("https://youtu.be/video-789"), "test-title"),
                        "s3://bucket/file.mp4",
                        "video/mp4");

                doThrow(new RecipeBriefingException(RecipeBriefingErrorCode.BRIEFING_CREATE_FAIL))
                        .when(recipeBriefingService)
                        .create(any(), any());
            }

            @Nested
            @DisplayName("When - 실행을 요청하면")
            class WhenRunning {

                @Test
                @DisplayName("Then - 진행 상태를 실패로 기록하고 예외를 던진다")
                void thenFailsProgressAndThrowsException() {
                    assertThatThrownBy(() -> sut.run(context))
                            .isInstanceOf(RecipeException.class)
                            .hasFieldOrPropertyWithValue("error", RecipeBriefingErrorCode.BRIEFING_CREATE_FAIL);

                    verify(recipeProgressService)
                            .failed(recipeId, RecipeProgressStep.BRIEFING, RecipeProgressDetail.BRIEFING);
                }
            }
        }
    }
}
