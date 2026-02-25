package com.cheftory.api.recipe.creation;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.cheftory.api.recipe.bookmark.RecipeBookmarkService;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmark;
import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.verify.exception.RecipeVerifyErrorCode;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaErrorCode;
import com.cheftory.api.recipe.creation.credit.RecipeCreditException;
import com.cheftory.api.recipe.creation.credit.RecipeCreditPort;
import com.cheftory.api.recipe.creation.pipeline.RecipeCreationExecutionContext;
import com.cheftory.api.recipe.creation.pipeline.RecipeCreationPipeline;
import com.cheftory.api.recipe.creation.progress.RecipeProgressService;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import com.cheftory.api.recipe.exception.RecipeException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@DisplayName("AsyncRecipeCreationService 테스트")
class AsyncRecipeCreationServiceTest {

    private RecipeProgressService recipeProgressService;
    private RecipeInfoService recipeInfoService;
    private RecipeBookmarkService recipeBookmarkService;
    private RecipeCreditPort creditPort;
    private RecipeCreationPipeline recipeCreationPipeline;
    private AsyncRecipeCreationService sut;

    @BeforeEach
    void setUp() {
        recipeProgressService = mock(RecipeProgressService.class);
        recipeInfoService = mock(RecipeInfoService.class);
        recipeBookmarkService = mock(RecipeBookmarkService.class);
        creditPort = mock(RecipeCreditPort.class);
        recipeCreationPipeline = mock(RecipeCreationPipeline.class);
        sut = new AsyncRecipeCreationService(
                recipeProgressService, recipeInfoService, recipeBookmarkService, creditPort, recipeCreationPipeline);
    }

    @Nested
    @DisplayName("create()")
    class Create {
        UUID recipeId;
        UUID jobId;
        long creditCost;
        String videoId;

        @BeforeEach
        void setUp() {
            recipeId = UUID.randomUUID();
            jobId = UUID.randomUUID();
            creditCost = 10L;
            videoId = "video-123";
        }

        @Nested
        @DisplayName("Given - 파이프라인이 성공하면")
        class GivenPipelineSuccess {
            @Test
            @DisplayName("Then - 파이프라인을 실행하고 실패 정리는 하지 않는다")
            void thenRunPipelineOnly() throws Exception {
                sut.create(recipeId, creditCost, videoId, jobId);

                ArgumentCaptor<RecipeCreationExecutionContext> captor =
                        ArgumentCaptor.forClass(RecipeCreationExecutionContext.class);
                verify(recipeCreationPipeline).run(captor.capture());
                RecipeCreationExecutionContext context = captor.getValue();
                org.assertj.core.api.Assertions.assertThat(context.getRecipeId())
                        .isEqualTo(recipeId);
                org.assertj.core.api.Assertions.assertThat(context.getVideoId()).isEqualTo(videoId);
                org.assertj.core.api.Assertions.assertThat(context.getJobId()).isEqualTo(jobId);

                verify(recipeInfoService, never()).failed(recipeId);
                verify(recipeInfoService, never()).banned(recipeId);
                verify(recipeProgressService, never())
                        .failed(recipeId, RecipeProgressStep.FINISHED, RecipeProgressDetail.FINISHED, jobId);
            }
        }

        @Nested
        @DisplayName("Given - NOT_COOK_VIDEO 예외가 발생하면")
        class GivenNotCookVideo {
            @BeforeEach
            void setUp() throws Exception {
                doThrow(new RecipeException(RecipeVerifyErrorCode.NOT_COOK_VIDEO))
                        .when(recipeCreationPipeline)
                        .run(org.mockito.ArgumentMatchers.any());
                doReturn(List.of(bookmark(UUID.randomUUID()), bookmark(UUID.randomUUID())))
                        .when(recipeBookmarkService)
                        .gets(recipeId);
            }

            @Test
            @DisplayName("Then - recipe를 BANNED 처리하고 정리/환불을 수행한다")
            void thenBanAndCleanup() throws Exception {
                sut.create(recipeId, creditCost, videoId, jobId);

                verify(recipeInfoService).banned(recipeId);
                verify(recipeProgressService)
                        .failed(recipeId, RecipeProgressStep.FINISHED, RecipeProgressDetail.FINISHED, jobId);
                verify(recipeBookmarkService).deletes(anyList());
                verify(creditPort, org.mockito.Mockito.times(2))
                        .refundRecipeCreate(
                                org.mockito.ArgumentMatchers.any(),
                                org.mockito.ArgumentMatchers.eq(recipeId),
                                org.mockito.ArgumentMatchers.eq(creditCost));
            }
        }

        @Nested
        @DisplayName("Given - YouTube 메타 NOT_FOUND 예외가 발생하면")
        class GivenYoutubeNotFound {
            @BeforeEach
            void setUp() throws Exception {
                doThrow(new RecipeException(YoutubeMetaErrorCode.YOUTUBE_META_VIDEO_NOT_FOUND))
                        .when(recipeCreationPipeline)
                        .run(org.mockito.ArgumentMatchers.any());
                doReturn(List.of()).when(recipeBookmarkService).gets(recipeId);
            }

            @Test
            @DisplayName("Then - BANNED 처리 경로를 탄다")
            void thenBanPath() throws Exception {
                sut.create(recipeId, creditCost, videoId, jobId);

                verify(recipeInfoService).banned(recipeId);
                verify(recipeInfoService, never()).failed(recipeId);
            }
        }

        @Nested
        @DisplayName("Given - YouTube 메타 NOT_EMBEDDABLE 예외가 발생하면")
        class GivenYoutubeNotEmbeddable {
            @BeforeEach
            void setUp() throws Exception {
                doThrow(new RecipeException(YoutubeMetaErrorCode.YOUTUBE_META_VIDEO_NOT_EMBEDDABLE))
                        .when(recipeCreationPipeline)
                        .run(org.mockito.ArgumentMatchers.any());
                doReturn(List.of()).when(recipeBookmarkService).gets(recipeId);
            }

            @Test
            @DisplayName("Then - BANNED 처리 경로를 탄다")
            void thenBanPath() throws Exception {
                sut.create(recipeId, creditCost, videoId, jobId);
                verify(recipeInfoService).banned(recipeId);
                verify(recipeInfoService, never()).failed(recipeId);
            }
        }

        @Nested
        @DisplayName("Given - YouTube 메타 API_ERROR 예외가 발생하면")
        class GivenYoutubeApiError {
            @BeforeEach
            void setUp() throws Exception {
                doThrow(new RecipeException(YoutubeMetaErrorCode.YOUTUBE_META_API_ERROR))
                        .when(recipeCreationPipeline)
                        .run(org.mockito.ArgumentMatchers.any());
                doReturn(List.of()).when(recipeBookmarkService).gets(recipeId);
            }

            @Test
            @DisplayName("Then - FAILED 처리 경로를 탄다")
            void thenFailedPath() throws Exception {
                sut.create(recipeId, creditCost, videoId, jobId);
                verify(recipeInfoService).failed(recipeId);
                verify(recipeInfoService, never()).banned(recipeId);
            }
        }

        @Nested
        @DisplayName("Given - banned 처리 중 RecipeInfoService 예외가 발생하면")
        class GivenBannedHandlingFailure {
            @BeforeEach
            void setUp() throws Exception {
                doThrow(new RecipeException(RecipeVerifyErrorCode.NOT_COOK_VIDEO))
                        .when(recipeCreationPipeline)
                        .run(org.mockito.ArgumentMatchers.any());
                doThrow(mock(com.cheftory.api.recipe.content.info.exception.RecipeInfoException.class))
                        .when(recipeInfoService)
                        .banned(recipeId);
            }

            @Test
            @DisplayName("Then - cleanup은 수행하지 않고 예외를 삼킨다")
            void thenSwallow() {
                sut.create(recipeId, creditCost, videoId, jobId);
                verify(recipeProgressService, never())
                        .failed(recipeId, RecipeProgressStep.FINISHED, RecipeProgressDetail.FINISHED, jobId);
                verify(recipeBookmarkService, never()).gets(recipeId);
            }
        }

        @Nested
        @DisplayName("Given - failed 처리 중 RecipeInfoService 예외가 발생하면")
        class GivenFailedHandlingFailure {
            @BeforeEach
            void setUp() throws Exception {
                doThrow(new RuntimeException("boom"))
                        .when(recipeCreationPipeline)
                        .run(org.mockito.ArgumentMatchers.any());
                doThrow(mock(com.cheftory.api.recipe.content.info.exception.RecipeInfoException.class))
                        .when(recipeInfoService)
                        .failed(recipeId);
            }

            @Test
            @DisplayName("Then - cleanup은 수행하지 않고 예외를 삼킨다")
            void thenSwallow() {
                sut.create(recipeId, creditCost, videoId, jobId);
                verify(recipeProgressService, never())
                        .failed(recipeId, RecipeProgressStep.FINISHED, RecipeProgressDetail.FINISHED, jobId);
                verify(recipeBookmarkService, never()).gets(recipeId);
            }
        }

        @Nested
        @DisplayName("Given - cleanup 대상 북마크가 없으면")
        class GivenNoBookmarksToCleanup {
            @BeforeEach
            void setUp() throws Exception {
                doThrow(new RuntimeException("boom"))
                        .when(recipeCreationPipeline)
                        .run(org.mockito.ArgumentMatchers.any());
                doReturn(List.of()).when(recipeBookmarkService).gets(recipeId);
            }

            @Test
            @DisplayName("Then - 환불/삭제 없이 진행 상태만 실패로 남긴다")
            void thenNoRefundOrDelete() throws Exception {
                sut.create(recipeId, creditCost, videoId, jobId);
                verify(recipeInfoService).failed(recipeId);
                verify(recipeBookmarkService).deletes(List.of());
                verify(creditPort, never())
                        .refundRecipeCreate(
                                org.mockito.ArgumentMatchers.any(),
                                org.mockito.ArgumentMatchers.any(),
                                org.mockito.ArgumentMatchers.anyLong());
            }
        }

        @Nested
        @DisplayName("Given - 예상치 못한 예외가 발생하면")
        class GivenUnexpectedException {
            @BeforeEach
            void setUp() throws Exception {
                doThrow(new RuntimeException("boom"))
                        .when(recipeCreationPipeline)
                        .run(org.mockito.ArgumentMatchers.any());
                doReturn(List.of(bookmark(UUID.randomUUID())))
                        .when(recipeBookmarkService)
                        .gets(recipeId);
                doThrow(mock(RecipeCreditException.class))
                        .when(creditPort)
                        .refundRecipeCreate(
                                org.mockito.ArgumentMatchers.any(),
                                org.mockito.ArgumentMatchers.eq(recipeId),
                                org.mockito.ArgumentMatchers.eq(creditCost));
            }

            @Test
            @DisplayName("Then - FAILED 처리 경로를 타고 환불 실패는 삼킨다")
            void thenFailAndSwallowRefundError() throws Exception {
                sut.create(recipeId, creditCost, videoId, jobId);

                verify(recipeInfoService).failed(recipeId);
                verify(recipeProgressService)
                        .failed(recipeId, RecipeProgressStep.FINISHED, RecipeProgressDetail.FINISHED, jobId);
                verify(recipeBookmarkService).deletes(anyList());
            }
        }
    }

    private RecipeBookmark bookmark(UUID userId) {
        RecipeBookmark bookmark = mock(RecipeBookmark.class);
        doReturn(UUID.randomUUID()).when(bookmark).getId();
        doReturn(userId).when(bookmark).getUserId();
        return bookmark;
    }
}
