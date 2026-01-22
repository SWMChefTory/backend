package com.cheftory.api.recipe.creation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.cheftory.api.recipe.content.briefing.RecipeBriefingService;
import com.cheftory.api.recipe.content.briefing.exception.RecipeBriefingErrorCode;
import com.cheftory.api.recipe.content.briefing.exception.RecipeBriefingException;
import com.cheftory.api.recipe.content.caption.RecipeCaptionService;
import com.cheftory.api.recipe.content.caption.entity.RecipeCaption;
import com.cheftory.api.recipe.content.caption.exception.RecipeCaptionErrorCode;
import com.cheftory.api.recipe.content.caption.exception.RecipeCaptionException;
import com.cheftory.api.recipe.content.detail.RecipeDetailService;
import com.cheftory.api.recipe.content.detail.entity.RecipeDetail;
import com.cheftory.api.recipe.content.detailMeta.RecipeDetailMetaService;
import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.ingredient.RecipeIngredientService;
import com.cheftory.api.recipe.content.step.RecipeStepService;
import com.cheftory.api.recipe.content.tag.RecipeTagService;
import com.cheftory.api.recipe.content.youtubemeta.RecipeYoutubeMetaService;
import com.cheftory.api.recipe.creation.credit.RecipeCreditPort;
import com.cheftory.api.recipe.creation.identify.RecipeIdentifyService;
import com.cheftory.api.recipe.creation.progress.RecipeProgressService;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import com.cheftory.api.recipe.history.RecipeHistoryService;
import com.cheftory.api.recipe.history.entity.RecipeHistory;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.core.task.AsyncTaskExecutor;

@DisplayName("AsyncRecipeService 테스트")
class AsyncRecipeCreationServiceTest {

    private RecipeCaptionService recipeCaptionService;
    private RecipeStepService recipeStepService;
    private RecipeIngredientService recipeIngredientService;
    private RecipeTagService recipeTagService;
    private RecipeDetailMetaService recipeDetailMetaService;
    private RecipeProgressService recipeProgressService;
    private RecipeInfoService recipeInfoService;
    private RecipeDetailService recipeDetailService;
    private RecipeYoutubeMetaService recipeYoutubeMetaService;
    private RecipeIdentifyService recipeIdentifyService;
    private RecipeBriefingService recipeBriefingService;
    private RecipeHistoryService recipeHistoryService;
    private RecipeCreditPort creditPort;

    private AsyncTaskExecutor directExecutor;

    private AsyncRecipeCreationService sut;

    @BeforeEach
    void setUp() {
        recipeCaptionService = mock(RecipeCaptionService.class);
        recipeStepService = mock(RecipeStepService.class);
        recipeIngredientService = mock(RecipeIngredientService.class);
        recipeTagService = mock(RecipeTagService.class);
        recipeDetailMetaService = mock(RecipeDetailMetaService.class);
        recipeProgressService = mock(RecipeProgressService.class);
        recipeInfoService = mock(RecipeInfoService.class);
        recipeDetailService = mock(RecipeDetailService.class);
        recipeYoutubeMetaService = mock(RecipeYoutubeMetaService.class);
        recipeIdentifyService = mock(RecipeIdentifyService.class);
        recipeBriefingService = mock(RecipeBriefingService.class);
        recipeHistoryService = mock(RecipeHistoryService.class);
        creditPort = mock(RecipeCreditPort.class);

        directExecutor = Runnable::run;

        sut = new AsyncRecipeCreationService(
                recipeCaptionService,
                recipeStepService,
                recipeIngredientService,
                recipeTagService,
                recipeDetailMetaService,
                recipeProgressService,
                recipeInfoService,
                recipeDetailService,
                recipeYoutubeMetaService,
                recipeIdentifyService,
                recipeBriefingService,
                recipeHistoryService,
                directExecutor,
                creditPort);
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
            private UUID captionId;
            private RecipeCaption caption;
            private RecipeDetail detail;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                creditCost = 100L;
                videoId = "vid-123";
                videoUrl = URI.create("https://youtu.be/vid-123");
                captionId = UUID.randomUUID();
                caption = mock(RecipeCaption.class);
                detail = mock(RecipeDetail.class);

                doReturn(List.of()).when(detail).ingredients();
                doReturn(List.of()).when(detail).tags();
                doReturn(15).when(detail).cookTime();
                doReturn(2).when(detail).servings();
                doReturn("설명").when(detail).description();

                doReturn(captionId).when(recipeCaptionService).create(videoId, recipeId);
                doReturn(caption).when(recipeCaptionService).get(captionId);
                doReturn(detail).when(recipeDetailService).getRecipeDetails(videoId, caption);
            }

            @Nested
            @DisplayName("When - create 메소드를 호출하면")
            class WhenCreateMethodCalled {

                @Test
                @DisplayName("Then - 올바른 순서로 서비스가 호출되고 성공 처리된다")
                void thenServicesCalledInCorrectOrderAndSuccessProcessed() {
                    sut.create(recipeId, creditCost, videoId, videoUrl);

                    InOrder inOrder = inOrder(recipeProgressService, recipeCaptionService);

                    inOrder.verify(recipeProgressService)
                            .create(recipeId, RecipeProgressStep.READY, RecipeProgressDetail.READY);

                    inOrder.verify(recipeCaptionService).create(videoId, recipeId);

                    inOrder.verify(recipeProgressService)
                            .create(recipeId, RecipeProgressStep.CAPTION, RecipeProgressDetail.CAPTION);

                    inOrder.verify(recipeCaptionService).get(captionId);

                    verify(recipeDetailService).getRecipeDetails(videoId, caption);

                    verify(recipeIngredientService).create(eq(recipeId), any());
                    verify(recipeProgressService)
                            .create(recipeId, RecipeProgressStep.DETAIL, RecipeProgressDetail.INGREDIENT);

                    verify(recipeTagService).create(eq(recipeId), any());
                    verify(recipeProgressService).create(recipeId, RecipeProgressStep.DETAIL, RecipeProgressDetail.TAG);

                    verify(recipeDetailMetaService).create(recipeId, 15, 2, "설명");
                    verify(recipeProgressService)
                            .create(recipeId, RecipeProgressStep.DETAIL, RecipeProgressDetail.DETAIL_META);

                    verify(recipeStepService).create(recipeId, caption);
                    verify(recipeProgressService).create(recipeId, RecipeProgressStep.STEP, RecipeProgressDetail.STEP);

                    verify(recipeBriefingService).create(videoId, recipeId);
                    verify(recipeProgressService)
                            .create(recipeId, RecipeProgressStep.BRIEFING, RecipeProgressDetail.BRIEFING);

                    verify(recipeInfoService).success(recipeId);
                    verify(recipeProgressService)
                            .create(recipeId, RecipeProgressStep.FINISHED, RecipeProgressDetail.FINISHED);

                    verify(recipeIdentifyService).delete(videoUrl);

                    verify(recipeInfoService, never()).failed(any());
                    verify(recipeHistoryService, never()).deleteByRecipe(any());
                    verifyNoInteractions(creditPort);

                    verifyNoMoreInteractions(recipeYoutubeMetaService);
                }
            }
        }
    }

    @Nested
    @DisplayName("예외 플로우")
    class ExceptionFlow {

        @Nested
        @DisplayName("NOT_COOK_RECIPE 예외")
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

                    doThrow(new RecipeCaptionException(RecipeCaptionErrorCode.NOT_COOK_RECIPE))
                            .when(recipeCaptionService)
                            .create(videoId, recipeId);

                    doReturn(List.of()).when(recipeHistoryService).deleteByRecipe(recipeId);
                }

                @Nested
                @DisplayName("When - create 메소드를 호출하면")
                class WhenCreateMethodCalled {

                    @Test
                    @DisplayName("Then - 실패 처리되고 ban 처리된다")
                    void thenFailedAndBanProcessed() {
                        sut.create(recipeId, creditCost, videoId, videoUrl);

                        verify(recipeInfoService).failed(recipeId);
                        verify(recipeHistoryService).deleteByRecipe(recipeId);
                        verify(recipeYoutubeMetaService).ban(recipeId);
                        verify(recipeIdentifyService).delete(videoUrl);

                        verify(creditPort, never()).refundRecipeCreate(any(), any(), anyLong());

                        verify(recipeInfoService, never()).success(any());
                        verify(recipeDetailService, never()).getRecipeDetails(any(), any());
                        verify(recipeStepService, never()).create(any(), any());
                    }
                }
            }
        }

        @Nested
        @DisplayName("CAPTION_CREATE_FAIL 예외")
        class CaptionCreateFailException {

            @Nested
            @DisplayName("Given - 캡션 생성이 실패할 때")
            class GivenCaptionCreationFail {

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

                    doThrow(new RecipeCaptionException(RecipeCaptionErrorCode.CAPTION_CREATE_FAIL))
                            .when(recipeCaptionService)
                            .create(videoId, recipeId);

                    doReturn(List.of()).when(recipeHistoryService).deleteByRecipe(recipeId);
                }

                @Nested
                @DisplayName("When - create 메소드를 호출하면")
                class WhenCreateMethodCalled {

                    @Test
                    @DisplayName("Then - 실패 처리만 되고 ban 처리는 되지 않는다")
                    void thenOnlyFailedProcessedWithoutBan() {
                        sut.create(recipeId, creditCost, videoId, videoUrl);

                        verify(recipeInfoService).failed(recipeId);
                        verify(recipeHistoryService).deleteByRecipe(recipeId);
                        verify(recipeIdentifyService).delete(videoUrl);

                        verify(recipeYoutubeMetaService, never()).ban(any());

                        verify(creditPort, never()).refundRecipeCreate(any(), any(), anyLong());

                        verify(recipeInfoService, never()).success(any());
                        verify(recipeDetailService, never()).getRecipeDetails(any(), any());
                        verify(recipeStepService, never()).create(any(), any());
                    }
                }
            }
        }

        @Nested
        @DisplayName("BRIEFING_CREATE_FAIL 예외")
        class BriefingCreateFailException {

            @Nested
            @DisplayName("Given - 브리핑 생성이 실패할 때")
            class GivenBriefingCreationFail {

                private UUID recipeId;
                private long creditCost;
                private String videoId;
                private URI videoUrl;
                private UUID captionId;
                private RecipeCaption caption;
                private RecipeDetail detail;

                @BeforeEach
                void setUp() {
                    recipeId = UUID.randomUUID();
                    creditCost = 100L;
                    videoId = "briefing-fail";
                    videoUrl = URI.create("https://youtu.be/briefing-fail");
                    captionId = UUID.randomUUID();
                    caption = mock(RecipeCaption.class);
                    detail = mock(RecipeDetail.class);

                    doReturn(List.of()).when(detail).ingredients();
                    doReturn(List.of()).when(detail).tags();
                    doReturn(15).when(detail).cookTime();
                    doReturn(2).when(detail).servings();
                    doReturn("설명").when(detail).description();

                    doReturn(captionId).when(recipeCaptionService).create(videoId, recipeId);
                    doReturn(caption).when(recipeCaptionService).get(captionId);
                    doReturn(detail).when(recipeDetailService).getRecipeDetails(videoId, caption);

                    doThrow(new RecipeBriefingException(RecipeBriefingErrorCode.BRIEFING_CREATE_FAIL))
                            .when(recipeBriefingService)
                            .create(videoId, recipeId);

                    doReturn(List.of()).when(recipeHistoryService).deleteByRecipe(recipeId);
                }

                @Nested
                @DisplayName("When - create 메소드를 호출하면")
                class WhenCreateMethodCalled {

                    @Test
                    @DisplayName("Then - 실패 처리되고 ban 처리는 되지 않는다")
                    void thenFailedProcessedWithoutBan() {
                        sut.create(recipeId, creditCost, videoId, videoUrl);

                        verify(recipeInfoService).failed(recipeId);
                        verify(recipeHistoryService).deleteByRecipe(recipeId);
                        verify(recipeIdentifyService).delete(videoUrl);

                        verify(recipeYoutubeMetaService, never()).ban(any());

                        verify(recipeInfoService, never()).success(any());

                        verify(recipeCaptionService).create(videoId, recipeId);
                        verify(recipeDetailService).getRecipeDetails(videoId, caption);
                        verify(recipeBriefingService).create(videoId, recipeId);

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
                            .when(recipeCaptionService)
                            .create(videoId, recipeId);

                    doReturn(List.of()).when(recipeHistoryService).deleteByRecipe(recipeId);
                }

                @Nested
                @DisplayName("When - create 메소드를 호출하면")
                class WhenCreateMethodCalled {

                    @Test
                    @DisplayName("Then - 실패 처리되고 식별자가 삭제된다")
                    void thenFailedProcessedAndIdentifierDeleted() {
                        sut.create(recipeId, creditCost, videoId, videoUrl);

                        verify(recipeInfoService).failed(recipeId);
                        verify(recipeHistoryService).deleteByRecipe(recipeId);
                        verify(recipeIdentifyService).delete(videoUrl);

                        verify(recipeYoutubeMetaService, never()).ban(any());
                        verify(recipeInfoService, never()).success(any());

                        verify(recipeDetailService, never()).getRecipeDetails(any(), any());
                        verify(recipeStepService, never()).create(any(), any());

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
        @DisplayName("Given - 실패 시 히스토리가 존재할 때")
        class GivenHistoriesOnFailure {

            private UUID recipeId;
            private long creditCost;
            private String videoId;
            private URI videoUrl;

            private UUID userId1;
            private UUID userId2;
            private RecipeHistory h1;
            private RecipeHistory h2;

            @BeforeEach
            void setUp() {
                recipeId = UUID.randomUUID();
                creditCost = 77L;
                videoId = "fail-with-histories";
                videoUrl = URI.create("https://youtu.be/fail-with-histories");

                doThrow(new RecipeCaptionException(RecipeCaptionErrorCode.CAPTION_CREATE_FAIL))
                        .when(recipeCaptionService)
                        .create(videoId, recipeId);

                userId1 = UUID.randomUUID();
                userId2 = UUID.randomUUID();

                h1 = mock(RecipeHistory.class);
                h2 = mock(RecipeHistory.class);
                doReturn(userId1).when(h1).getUserId();
                doReturn(userId2).when(h2).getUserId();

                doReturn(List.of(h1, h2)).when(recipeHistoryService).deleteByRecipe(recipeId);
            }

            @Test
            @DisplayName("Then - 삭제된 히스토리 수만큼 환불이 지급된다")
            void thenRefundGrantedForEachHistory() {
                sut.create(recipeId, creditCost, videoId, videoUrl);

                verify(recipeInfoService).failed(recipeId);
                verify(recipeHistoryService).deleteByRecipe(recipeId);

                verify(creditPort).refundRecipeCreate(userId1, recipeId, creditCost);
                verify(creditPort).refundRecipeCreate(userId2, recipeId, creditCost);
                verifyNoMoreInteractions(creditPort);
            }
        }
    }
}
