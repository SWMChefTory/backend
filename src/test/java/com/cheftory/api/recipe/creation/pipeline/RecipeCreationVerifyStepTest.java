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

@DisplayName("RecipeCreationVerifyStep 테스트")
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
    @DisplayName("실행 (run)")
    class Run {

        @Nested
        @DisplayName("Given - 검증이 성공할 때")
        class GivenSuccess {
            UUID recipeId;
            String videoId;
            RecipeCreationExecutionContext context;
            String fileUri;
            String mimeType;

            @BeforeEach
            void setUp() throws RecipeVerifyException {
                recipeId = UUID.randomUUID();
                videoId = "video-123";
                context = RecipeCreationExecutionContext.of(
                        recipeId, videoId, URI.create("https://youtu.be/video-123"), "test-title");
                fileUri = "s3://bucket/file.mp4";
                mimeType = "video/mp4";

                when(recipeVerifyService.verify(videoId)).thenReturn(new RecipeVerifyClientResponse(fileUri, mimeType));
            }

            @Nested
            @DisplayName("When - 실행을 요청하면")
            class WhenRunning {
                RecipeCreationExecutionContext result;

                @BeforeEach
                void setUp() throws RecipeException {
                    result = sut.run(context);
                }

                @Test
                @DisplayName("Then - 검증을 수행하고 파일 정보가 포함된 컨텍스트를 반환한다")
                void thenVerifiesAndReturnsContext() throws RecipeVerifyException {
                    InOrder order = inOrder(recipeProgressService);
                    order.verify(recipeProgressService)
                            .start(recipeId, RecipeProgressStep.CAPTION, RecipeProgressDetail.CAPTION);
                    order.verify(recipeProgressService)
                            .success(recipeId, RecipeProgressStep.CAPTION, RecipeProgressDetail.CAPTION);

                    verify(recipeVerifyService).verify(videoId);

                    assertThat(result.getFileUri()).isEqualTo(fileUri);
                    assertThat(result.getMimeType()).isEqualTo(mimeType);
                }
            }
        }

        @Nested
        @DisplayName("Given - 검증 중 예외가 발생할 때")
        class GivenException {
            UUID recipeId;
            String videoId;
            RecipeCreationExecutionContext context;

            @BeforeEach
            void setUp() throws RecipeVerifyException {
                recipeId = UUID.randomUUID();
                videoId = "video-456";
                context = RecipeCreationExecutionContext.of(
                        recipeId, videoId, URI.create("https://youtu.be/video-456"), "test-title");

                when(recipeVerifyService.verify(videoId))
                        .thenThrow(new RecipeVerifyException(RecipeVerifyErrorCode.SERVER_ERROR));
            }

            @Nested
            @DisplayName("When - 실행을 요청하면")
            class WhenRunning {

                @Test
                @DisplayName("Then - 진행 상태를 실패로 기록하고 예외를 던진다")
                void thenFailsProgressAndThrowsException() throws RecipeVerifyException {
                    assertThatThrownBy(() -> sut.run(context))
                            .isInstanceOf(RecipeException.class)
                            .hasFieldOrPropertyWithValue("error", RecipeVerifyErrorCode.SERVER_ERROR);

                    verify(recipeProgressService)
                            .failed(recipeId, RecipeProgressStep.CAPTION, RecipeProgressDetail.CAPTION);
                    verify(recipeVerifyService).verify(videoId);
                }
            }
        }
    }
}
