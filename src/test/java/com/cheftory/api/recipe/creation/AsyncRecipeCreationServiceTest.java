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

import com.cheftory.api.recipe.bookmark.RecipeBookmarkService;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmark;
import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.verify.exception.RecipeVerifyErrorCode;
import com.cheftory.api.recipe.content.verify.exception.RecipeVerifyException;
import com.cheftory.api.recipe.content.youtubemeta.RecipeYoutubeMetaService;
import com.cheftory.api.recipe.creation.credit.RecipeCreditPort;
import com.cheftory.api.recipe.creation.identify.RecipeIdentifyService;
import com.cheftory.api.recipe.creation.pipeline.RecipeCreationExecutionContext;
import com.cheftory.api.recipe.creation.pipeline.RecipeCreationPipeline;
import com.cheftory.api.recipe.creation.progress.RecipeProgressService;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("AsyncRecipeService 테스트")
class AsyncRecipeCreationServiceTest {

    private RecipeProgressService recipeProgressService;
    private RecipeInfoService recipeInfoService;
    private RecipeYoutubeMetaService recipeYoutubeMetaService;
    private RecipeIdentifyService recipeIdentifyService;
    private RecipeBookmarkService recipeBookmarkService;
    private RecipeCreditPort creditPort;
    private RecipeCreationPipeline recipeCreationPipeline;

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
    @DisplayName("성공 플로우")
    class SuccessFlow {

        @Nested
        @DisplayName("Given - 유효한 인자가 주어졌을 때")
        class GivenValidArguments {

            private UUID recipeId;
            private long creditCost;
            private String videoId;
            private URI videoUrl;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                creditCost = 100L;
                videoId = "vid-123";
                videoUrl = URI.create("https://youtu.be/vid-123");
            }

            @Nested
            @DisplayName("When - create 메소드를 호출하면")
            class WhenCreateMethodCalled {

                @Test
                @DisplayName("Then - 파이프라인이 실행되고 identify가 삭제된다")
                void thenPipelineRunAndIdentifyDeleted() {
                    sut.create(recipeId, creditCost, videoId, videoUrl);

                    verify(recipeCreationPipeline).run(any(RecipeCreationExecutionContext.class));
                    verify(recipeIdentifyService).delete(videoUrl);

                    verify(recipeInfoService, never()).failed(any());
                    verify(recipeProgressService, never()).failed(any(), any(), any());
                    verify(recipeBookmarkService, never()).deleteByRecipe(any());
                    verifyNoInteractions(creditPort);
                }
            }
        }
    }

    @Nested
    @DisplayName("예외 플로우")
    class ExceptionFlow {

        @Nested
        @DisplayName("NOT_COOK_VIDEO 예외")
        class NotCookRecipeException {

            @Nested
            @DisplayName("Given - 요리 레시피가 아닌 영상일 때")
            class GivenNotCookVideo {

                private UUID recipeId;
                private long creditCost;
                private String videoId;
                private URI videoUrl;

                @BeforeEach
                void setUp() {
                    recipeId = UUID.randomUUID();
                    creditCost = 100L;
                    videoId = "not-cook";
                    videoUrl = URI.create("https://youtu.be/not-cook");

                    doThrow(new RecipeVerifyException(RecipeVerifyErrorCode.NOT_COOK_VIDEO))
                            .when(recipeCreationPipeline)
                            .run(any(RecipeCreationExecutionContext.class));

                    doReturn(List.of()).when(recipeBookmarkService).deleteByRecipe(recipeId);
                }

                @Nested
                @DisplayName("When - create 메소드를 호출하면")
                class WhenCreateMethodCalled {

                    @Test
                    @DisplayName("Then - 실패 처리되고 ban 처리된다")
                    void thenFailedAndBanProcessed() {
                        sut.create(recipeId, creditCost, videoId, videoUrl);

                        verify(recipeInfoService).failed(recipeId);
                        verify(recipeProgressService)
                                .failed(recipeId, RecipeProgressStep.FINISHED, RecipeProgressDetail.FINISHED);
                        verify(recipeBookmarkService).deleteByRecipe(recipeId);
                        verify(recipeYoutubeMetaService, never()).ban(any());
                        verify(recipeIdentifyService).delete(videoUrl);

                        verify(creditPort, never()).refundRecipeCreate(any(), any(), anyLong());
                    }
                }
            }
        }

        @Nested
        @DisplayName("VERIFY_SERVER_ERROR 예외")
        class VerifyServerErrorException {

            @Nested
            @DisplayName("Given - verify 호출이 실패할 때")
            class GivenVerifyFail {

                private UUID recipeId;
                private long creditCost;
                private String videoId;
                private URI videoUrl;

                @BeforeEach
                void setUp() {
                    recipeId = UUID.randomUUID();
                    creditCost = 100L;
                    videoId = "no-meta";
                    videoUrl = URI.create("https://youtu.be/no-meta");

                    doThrow(new RecipeVerifyException(RecipeVerifyErrorCode.SERVER_ERROR))
                            .when(recipeCreationPipeline)
                            .run(any(RecipeCreationExecutionContext.class));

                    doReturn(List.of()).when(recipeBookmarkService).deleteByRecipe(recipeId);
                }

                @Nested
                @DisplayName("When - create 메소드를 호출하면")
                class WhenCreateMethodCalled {

                    @Test
                    @DisplayName("Then - 실패 처리만 되고 ban 처리는 되지 않는다")
                    void thenOnlyFailedProcessedWithoutBan() {
                        sut.create(recipeId, creditCost, videoId, videoUrl);

                        verify(recipeInfoService).failed(recipeId);
                        verify(recipeProgressService)
                                .failed(recipeId, RecipeProgressStep.FINISHED, RecipeProgressDetail.FINISHED);
                        verify(recipeBookmarkService).deleteByRecipe(recipeId);
                        verify(recipeIdentifyService).delete(videoUrl);

                        verify(recipeYoutubeMetaService, never()).ban(any());
                        verify(creditPort, never()).refundRecipeCreate(any(), any(), anyLong());
                    }
                }
            }
        }

        @Nested
        @DisplayName("일반 RuntimeException")
        class GenericRuntimeException {

            @Nested
            @DisplayName("Given - 일반 예외가 발생할 때")
            class GivenGenericException {

                private UUID recipeId;
                private long creditCost;
                private String videoId;
                private URI videoUrl;

                @BeforeEach
                void setUp() {
                    recipeId = UUID.randomUUID();
                    creditCost = 100L;
                    videoId = "boom";
                    videoUrl = URI.create("https://youtu.be/boom");

                    doThrow(new RuntimeException("boom"))
                            .when(recipeCreationPipeline)
                            .run(any(RecipeCreationExecutionContext.class));

                    doReturn(List.of()).when(recipeBookmarkService).deleteByRecipe(recipeId);
                }

                @Nested
                @DisplayName("When - create 메소드를 호출하면")
                class WhenCreateMethodCalled {

                    @Test
                    @DisplayName("Then - 실패 처리되고 식별자가 삭제된다")
                    void thenFailedProcessedAndIdentifierDeleted() {
                        sut.create(recipeId, creditCost, videoId, videoUrl);

                        verify(recipeInfoService).failed(recipeId);
                        verify(recipeProgressService)
                                .failed(recipeId, RecipeProgressStep.FINISHED, RecipeProgressDetail.FINISHED);
                        verify(recipeBookmarkService).deleteByRecipe(recipeId);
                        verify(recipeIdentifyService).delete(videoUrl);

                        verify(recipeYoutubeMetaService, never()).ban(any());
                        verify(creditPort, never()).refundRecipeCreate(any(), any(), anyLong());
                    }
                }
            }
        }
    }

    @Nested
    @DisplayName("환불(credit) 처리")
    class RefundFlow {

        @Nested
        @DisplayName("Given - 실패 시 북마크가 존재할 때")
        class GivenBookmarksOnFailure {

            private UUID recipeId;
            private long creditCost;
            private String videoId;
            private URI videoUrl;

            private UUID userId1;
            private UUID userId2;
            private RecipeBookmark bookmark1;
            private RecipeBookmark bookmark2;

            @BeforeEach
            void setUp() {
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
                        .deleteByRecipe(recipeId);
            }

            @Test
            @DisplayName("Then - 삭제된 북마크 수만큼 환불이 지급된다")
            void thenRefundGrantedForEachBookmark() {
                sut.create(recipeId, creditCost, videoId, videoUrl);

                verify(recipeInfoService).failed(recipeId);
                verify(recipeBookmarkService).deleteByRecipe(recipeId);

                verify(creditPort).refundRecipeCreate(userId1, recipeId, creditCost);
                verify(creditPort).refundRecipeCreate(userId2, recipeId, creditCost);
                verifyNoMoreInteractions(creditPort);
            }
        }
    }
}
