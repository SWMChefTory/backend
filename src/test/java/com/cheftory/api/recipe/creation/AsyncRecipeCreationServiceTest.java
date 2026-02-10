package com.cheftory.api.recipe.creation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.cheftory.api.credit.exception.CreditException;
import com.cheftory.api.recipe.bookmark.RecipeBookmarkService;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmark;
import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoException;
import com.cheftory.api.recipe.content.verify.RecipeVerifyService;
import com.cheftory.api.recipe.content.verify.exception.RecipeVerifyErrorCode;
import com.cheftory.api.recipe.content.verify.exception.RecipeVerifyException;
import com.cheftory.api.recipe.content.youtubemeta.RecipeYoutubeMetaService;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaException;
import com.cheftory.api.recipe.creation.credit.RecipeCreditPort;
import com.cheftory.api.recipe.creation.identify.RecipeIdentifyService;
import com.cheftory.api.recipe.creation.pipeline.RecipeCreationExecutionContext;
import com.cheftory.api.recipe.creation.pipeline.RecipeCreationPipeline;
import com.cheftory.api.recipe.creation.progress.RecipeProgressService;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import com.cheftory.api.recipe.exception.RecipeException;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("AsyncRecipeCreationService 테스트")
class AsyncRecipeCreationServiceTest {

    private RecipeProgressService recipeProgressService;
    private RecipeInfoService recipeInfoService;
    private RecipeYoutubeMetaService recipeYoutubeMetaService;
    private RecipeIdentifyService recipeIdentifyService;
    private RecipeBookmarkService recipeBookmarkService;
    private RecipeCreditPort creditPort;
    private RecipeCreationPipeline recipeCreationPipeline;
    private RecipeVerifyService recipeVerifyService;

    private AsyncRecipeCreationService sut;

    @BeforeEach
    void setUp() {
        recipeProgressService = mock(RecipeProgressService.class);
        recipeInfoService = mock(RecipeInfoService.class);
        recipeYoutubeMetaService = mock(RecipeYoutubeMetaService.class);
        recipeIdentifyService = mock(RecipeIdentifyService.class);
        recipeBookmarkService = mock(RecipeBookmarkService.class);
        creditPort = mock(RecipeCreditPort.class);
        recipeCreationPipeline = mock(RecipeCreationPipeline.class);

        sut = new AsyncRecipeCreationService(
                recipeProgressService,
                recipeInfoService,
                recipeYoutubeMetaService,
                recipeIdentifyService,
                recipeBookmarkService,
                creditPort,
                recipeCreationPipeline);
    }

    @Nested
    @DisplayName("레시피 생성 (create)")
    class Create {

        @Nested
        @DisplayName("Given - 유효한 인자가 주어졌을 때")
        class GivenValidArguments {
            UUID recipeId;
            long creditCost;
            String videoId;
            URI videoUrl;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                creditCost = 100L;
                videoId = "vid-123";
                videoUrl = URI.create("https://youtu.be/vid-123");
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @BeforeEach
                void setUp() throws RecipeException {
                    sut.create(recipeId, creditCost, videoId, videoUrl, null);
                }

                @Test
                @DisplayName("Then - 파이프라인을 실행하고 식별자를 삭제한다")
                void thenRunsPipelineAndDeletesIdentifier() throws RecipeException {
                    verify(recipeCreationPipeline).run(any(RecipeCreationExecutionContext.class));
                    verify(recipeIdentifyService).delete(videoUrl);

                    verify(recipeInfoService, never()).failed(any());
                    verify(recipeProgressService, never()).failed(any(), any(), any());
                    verify(recipeBookmarkService, never()).deletes(any());
                    verifyNoInteractions(creditPort);
                }
            }
        }

        @Nested
        @DisplayName("Given - 요리 영상이 아닐 때")
        class GivenNotCookVideo {
            UUID recipeId;
            long creditCost;
            String videoId;
            URI videoUrl;

            @BeforeEach
            void setUp() throws RecipeException {
                recipeId = UUID.randomUUID();
                creditCost = 100L;
                videoId = "not-cook";
                videoUrl = URI.create("https://youtu.be/not-cook");

                doThrow(new RecipeVerifyException(RecipeVerifyErrorCode.NOT_COOK_VIDEO))
                        .when(recipeCreationPipeline)
                        .run(any(RecipeCreationExecutionContext.class));

                doReturn(List.of()).when(recipeBookmarkService).gets(recipeId);
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @BeforeEach
                void setUp() throws RecipeInfoException, YoutubeMetaException {
                    sut.create(recipeId, creditCost, videoId, videoUrl, null);
                }

                @Test
                @DisplayName("Then - 실패 처리하고 메타를 밴 처리한다")
                void thenFailsAndBansMeta() throws RecipeInfoException, YoutubeMetaException, CreditException {
                    verify(recipeInfoService).failed(recipeId);
                    verify(recipeProgressService)
                            .failed(recipeId, RecipeProgressStep.FINISHED, RecipeProgressDetail.FINISHED);
                    verify(recipeBookmarkService).gets(recipeId);
                    verify(recipeYoutubeMetaService).ban(recipeId);
                    verify(recipeIdentifyService).delete(videoUrl);

                    verify(creditPort, never()).refundRecipeCreate(any(), any(), anyLong());
                }
            }
        }

        @Nested
        @DisplayName("Given - 검증 서버 오류가 발생했을 때")
        class GivenVerifyServerError {
            UUID recipeId;
            long creditCost;
            String videoId;
            URI videoUrl;

            @BeforeEach
            void setUp() throws RecipeException {
                recipeId = UUID.randomUUID();
                creditCost = 100L;
                videoId = "no-meta";
                videoUrl = URI.create("https://youtu.be/no-meta");

                doThrow(new RecipeVerifyException(RecipeVerifyErrorCode.SERVER_ERROR))
                        .when(recipeCreationPipeline)
                        .run(any(RecipeCreationExecutionContext.class));

                doReturn(List.of()).when(recipeBookmarkService).gets(recipeId);
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @BeforeEach
                void setUp() throws RecipeInfoException, YoutubeMetaException {
                    sut.create(recipeId, creditCost, videoId, videoUrl, null);
                }

                @Test
                @DisplayName("Then - 실패 처리만 하고 밴 처리는 하지 않는다")
                void thenFailsWithoutBan() throws RecipeInfoException, YoutubeMetaException, CreditException {
                    verify(recipeInfoService).failed(recipeId);
                    verify(recipeProgressService)
                            .failed(recipeId, RecipeProgressStep.FINISHED, RecipeProgressDetail.FINISHED);
                    verify(recipeBookmarkService).gets(recipeId);
                    verify(recipeIdentifyService).delete(videoUrl);

                    verify(recipeYoutubeMetaService, never()).ban(any());
                    verify(recipeYoutubeMetaService).failed(recipeId);
                    verify(creditPort, never()).refundRecipeCreate(any(), any(), anyLong());
                }
            }
        }

        @Nested
        @DisplayName("Given - 일반 예외가 발생했을 때")
        class GivenGenericException {
            UUID recipeId;
            long creditCost;
            String videoId;
            URI videoUrl;

            @BeforeEach
            void setUp() throws RecipeException {
                recipeId = UUID.randomUUID();
                creditCost = 100L;
                videoId = "boom";
                videoUrl = URI.create("https://youtu.be/boom");

                doThrow(new RuntimeException("boom"))
                        .when(recipeCreationPipeline)
                        .run(any(RecipeCreationExecutionContext.class));

                doReturn(List.of()).when(recipeBookmarkService).gets(recipeId);
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @BeforeEach
                void setUp() throws RecipeInfoException, YoutubeMetaException {
                    sut.create(recipeId, creditCost, videoId, videoUrl, null);
                }

                @Test
                @DisplayName("Then - 실패 처리하고 식별자를 삭제한다")
                void thenFailsAndDeletesIdentifier() throws RecipeInfoException, YoutubeMetaException, CreditException {
                    verify(recipeInfoService).failed(recipeId);
                    verify(recipeProgressService)
                            .failed(recipeId, RecipeProgressStep.FINISHED, RecipeProgressDetail.FINISHED);
                    verify(recipeBookmarkService).gets(recipeId);
                    verify(recipeIdentifyService).delete(videoUrl);

                    verify(recipeYoutubeMetaService, never()).ban(any());
                    verify(recipeYoutubeMetaService).failed(recipeId);
                    verify(creditPort, never()).refundRecipeCreate(any(), any(), anyLong());
                }
            }
        }

        @Nested
        @DisplayName("Given - 실패 시 북마크가 존재할 때")
        class GivenBookmarksOnFailure {
            UUID recipeId;
            long creditCost;
            String videoId;
            URI videoUrl;
            UUID userId1;
            UUID userId2;
            RecipeBookmark bookmark1;
            RecipeBookmark bookmark2;

            @BeforeEach
            void setUp() throws RecipeException {
                recipeId = UUID.randomUUID();
                creditCost = 77L;
                videoId = "fail-with-histories";
                videoUrl = URI.create("https://youtu.be/fail-with-histories");

                doThrow(new RecipeVerifyException(RecipeVerifyErrorCode.SERVER_ERROR))
                        .when(recipeCreationPipeline)
                        .run(any(RecipeCreationExecutionContext.class));

                userId1 = UUID.randomUUID();
                userId2 = UUID.randomUUID();

                bookmark1 = mock(RecipeBookmark.class);
                bookmark2 = mock(RecipeBookmark.class);
                doReturn(userId1).when(bookmark1).getUserId();
                doReturn(userId2).when(bookmark2).getUserId();

                doReturn(List.of(bookmark1, bookmark2))
                        .when(recipeBookmarkService)
                        .gets(recipeId);
            }

            @Nested
            @DisplayName("When - 생성을 요청하면")
            class WhenCreating {

                @BeforeEach
                void setUp() throws RecipeInfoException, YoutubeMetaException {
                    sut.create(recipeId, creditCost, videoId, videoUrl, null);
                }

                @Test
                @DisplayName("Then - 각 북마크에 대해 환불을 진행한다")
                void thenRefundsForEachBookmark() throws CreditException {
                    verify(creditPort).refundRecipeCreate(userId1, recipeId, creditCost);
                    verify(creditPort).refundRecipeCreate(userId2, recipeId, creditCost);
                    verifyNoMoreInteractions(creditPort);
                }
            }
        }
    }
}
