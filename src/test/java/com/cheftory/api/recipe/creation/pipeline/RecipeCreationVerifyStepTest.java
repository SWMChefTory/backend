package com.cheftory.api.recipe.creation.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cheftory.api.recipe.content.verify.RecipeVerifyService;
import com.cheftory.api.recipe.content.verify.dto.RecipeVerifyClientResponse;
import com.cheftory.api.recipe.content.verify.exception.RecipeVerifyErrorCode;
import com.cheftory.api.recipe.content.verify.exception.RecipeVerifyException;
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
import org.mockito.InOrder;

@DisplayName("RecipeCreationVerifyStep")
class RecipeCreationVerifyStepTest {

    private RecipeVerifyService recipeVerifyService;
    private RecipeProgressService recipeProgressService;
    private RecipeCreationVerifyStep sut;

    @BeforeEach
    void setUp() {
        recipeVerifyService = mock(RecipeVerifyService.class);
        recipeProgressService = mock(RecipeProgressService.class);
        sut = createStep();
    }

    private RecipeCreationVerifyStep createStep() {
        try {
            Constructor<RecipeCreationVerifyStep> ctor = RecipeCreationVerifyStep.class.getDeclaredConstructor(
                    RecipeVerifyService.class, RecipeProgressService.class);
            ctor.setAccessible(true);
            return ctor.newInstance(recipeVerifyService, recipeProgressService);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to create RecipeCreationVerifyStep", ex);
        }
    }

    @Nested
    @DisplayName("run")
    class Run {

        @Test
        @DisplayName("성공 시 verify를 호출하고 fileUri, mimeType을 포함한 context를 반환한다")
        void shouldVerifyAndReturnUpdatedContext() throws RecipeVerifyException {
            UUID recipeId = UUID.randomUUID();
            String videoId = "video-123";
            URI videoUrl = URI.create("https://youtu.be/video-123");
            RecipeCreationExecutionContext context = RecipeCreationExecutionContext.of(recipeId, videoId, videoUrl);

            String fileUri = "s3://bucket/file.mp4";
            String mimeType = "video/mp4";
            RecipeVerifyClientResponse verifyResponse = new RecipeVerifyClientResponse(fileUri, mimeType);

            when(recipeVerifyService.verify(videoId)).thenReturn(verifyResponse);

            RecipeCreationExecutionContext result = sut.run(context);

            InOrder order = inOrder(recipeProgressService);
            order.verify(recipeProgressService)
                    .start(recipeId, RecipeProgressStep.CAPTION, RecipeProgressDetail.CAPTION);
            order.verify(recipeProgressService)
                    .success(recipeId, RecipeProgressStep.CAPTION, RecipeProgressDetail.CAPTION);

            verify(recipeVerifyService).verify(videoId);

            assertThat(result.getFileUri()).isEqualTo(fileUri);
            assertThat(result.getMimeType()).isEqualTo(mimeType);
        }

        @Test
        @DisplayName("예외 발생 시 progress를 failed로 기록하고 예외를 전파한다")
        void shouldFailProgressWhenExceptionThrown() throws RecipeVerifyException {
            UUID recipeId = UUID.randomUUID();
            String videoId = "video-456";
            URI videoUrl = URI.create("https://youtu.be/video-456");
            RecipeCreationExecutionContext context = RecipeCreationExecutionContext.of(recipeId, videoId, videoUrl);

            when(recipeVerifyService.verify(videoId))
                    .thenThrow(new RecipeVerifyException(RecipeVerifyErrorCode.SERVER_ERROR));

            assertThatThrownBy(() -> sut.run(context))
                    .isInstanceOf(RecipeException.class)
                    .hasFieldOrPropertyWithValue("error", RecipeVerifyErrorCode.SERVER_ERROR);

            verify(recipeProgressService).failed(recipeId, RecipeProgressStep.CAPTION, RecipeProgressDetail.CAPTION);
            verify(recipeVerifyService).verify(videoId);
        }
    }
}
