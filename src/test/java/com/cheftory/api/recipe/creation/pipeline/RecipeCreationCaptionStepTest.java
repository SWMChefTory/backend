package com.cheftory.api.recipe.creation.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cheftory.api.recipe.content.caption.RecipeCaptionService;
import com.cheftory.api.recipe.content.caption.entity.RecipeCaption;
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
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("RecipeCreationCaptionStep")
class RecipeCreationCaptionStepTest {

    private RecipeCaptionService recipeCaptionService;
    private RecipeProgressService recipeProgressService;
    private RecipeCreationCaptionStep sut;

    @BeforeEach
    void setUp() {
        recipeCaptionService = mock(RecipeCaptionService.class);
        recipeProgressService = mock(RecipeProgressService.class);
        sut = createStep();
    }

    private RecipeCreationCaptionStep createStep() {
        try {
            Constructor<RecipeCreationCaptionStep> ctor = RecipeCreationCaptionStep.class.getDeclaredConstructor(
                    RecipeCaptionService.class, RecipeProgressService.class);
            ctor.setAccessible(true);
            return ctor.newInstance(recipeCaptionService, recipeProgressService);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to create RecipeCreationCaptionStep", ex);
        }
    }

    @Nested
    @DisplayName("run")
    class Run {

        @Test
        @DisplayName("성공 시 캡션을 저장하고 progress를 갱신한다")
        void shouldCreateCaptionAndUpdateProgress() {
            UUID recipeId = UUID.randomUUID();
            String videoId = "video-123";
            URI videoUrl = URI.create("https://youtu.be/video-123");
            RecipeCreationExecutionContext context = RecipeCreationExecutionContext.of(recipeId, videoId, videoUrl);
            UUID captionId = UUID.randomUUID();
            RecipeCaption caption = mock(RecipeCaption.class);

            when(recipeCaptionService.create(videoId, recipeId)).thenReturn(captionId);
            when(recipeCaptionService.get(captionId)).thenReturn(caption);

            RecipeCreationExecutionContext result = sut.run(context);

            InOrder order = inOrder(recipeProgressService);
            order.verify(recipeProgressService)
                    .start(recipeId, RecipeProgressStep.CAPTION, RecipeProgressDetail.CAPTION);
            order.verify(recipeProgressService)
                    .success(recipeId, RecipeProgressStep.CAPTION, RecipeProgressDetail.CAPTION);

            verify(recipeCaptionService).create(videoId, recipeId);
            verify(recipeCaptionService).get(captionId);

            assertThat(ReflectionTestUtils.getField(result, "caption")).isEqualTo(caption);
        }

        @Test
        @DisplayName("예외 발생 시 progress를 failed로 기록하고 예외를 전파한다")
        void shouldFailProgressWhenExceptionThrown() {
            UUID recipeId = UUID.randomUUID();
            String videoId = "video-456";
            URI videoUrl = URI.create("https://youtu.be/video-456");
            RecipeCreationExecutionContext context = RecipeCreationExecutionContext.of(recipeId, videoId, videoUrl);

            when(recipeCaptionService.create(videoId, recipeId))
                    .thenThrow(new RecipeException(RecipeErrorCode.RECIPE_CREATE_FAIL));

            assertThatThrownBy(() -> sut.run(context))
                    .isInstanceOf(RecipeException.class)
                    .hasFieldOrPropertyWithValue("errorMessage", RecipeErrorCode.RECIPE_CREATE_FAIL);

            verify(recipeProgressService).failed(recipeId, RecipeProgressStep.CAPTION, RecipeProgressDetail.CAPTION);
            verify(recipeCaptionService).create(videoId, recipeId);
        }
    }
}
