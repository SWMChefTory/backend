package com.cheftory.api.recipeinfo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.cheftory.api.recipeinfo.briefing.RecipeBriefingService;
import com.cheftory.api.recipeinfo.briefing.exception.RecipeBriefingErrorCode;
import com.cheftory.api.recipeinfo.briefing.exception.RecipeBriefingException;
import com.cheftory.api.recipeinfo.caption.RecipeCaptionService;
import com.cheftory.api.recipeinfo.caption.entity.RecipeCaption;
import com.cheftory.api.recipeinfo.caption.exception.RecipeCaptionErrorCode;
import com.cheftory.api.recipeinfo.caption.exception.RecipeCaptionException;
import com.cheftory.api.recipeinfo.detail.RecipeDetail;
import com.cheftory.api.recipeinfo.detail.RecipeDetailService;
import com.cheftory.api.recipeinfo.detailMeta.RecipeDetailMetaService;
import com.cheftory.api.recipeinfo.identify.RecipeIdentifyService;
import com.cheftory.api.recipeinfo.ingredient.RecipeIngredientService;
import com.cheftory.api.recipeinfo.progress.RecipeProgressDetail;
import com.cheftory.api.recipeinfo.progress.RecipeProgressService;
import com.cheftory.api.recipeinfo.progress.RecipeProgressStep;
import com.cheftory.api.recipeinfo.recipe.RecipeService;
import com.cheftory.api.recipeinfo.step.RecipeStepService;
import com.cheftory.api.recipeinfo.tag.RecipeTagService;
import com.cheftory.api.recipeinfo.youtubemeta.RecipeYoutubeMetaService;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

@DisplayName("AsyncRecipeInfoCreationService 테스트")
class AsyncRecipeInfoCreationServiceTest {

  private RecipeCaptionService recipeCaptionService;
  private RecipeStepService recipeStepService;
  private RecipeIngredientService recipeIngredientService;
  private RecipeTagService recipeTagService;
  private RecipeDetailMetaService recipeDetailMetaService;
  private RecipeProgressService recipeProgressService;
  private RecipeService recipeService;
  private RecipeDetailService recipeDetailService;
  private RecipeYoutubeMetaService recipeYoutubeMetaService;
  private RecipeIdentifyService recipeIdentifyService;
  private RecipeBriefingService recipeBriefingService;

  private Executor directExecutor;

  private AsyncRecipeInfoCreationService sut;

  @BeforeEach
  void setUp() {
    recipeCaptionService = mock(RecipeCaptionService.class);
    recipeStepService = mock(RecipeStepService.class);
    recipeIngredientService = mock(RecipeIngredientService.class);
    recipeTagService = mock(RecipeTagService.class);
    recipeDetailMetaService = mock(RecipeDetailMetaService.class);
    recipeProgressService = mock(RecipeProgressService.class);
    recipeService = mock(RecipeService.class);
    recipeDetailService = mock(RecipeDetailService.class);
    recipeYoutubeMetaService = mock(RecipeYoutubeMetaService.class);
    recipeIdentifyService = mock(RecipeIdentifyService.class);
    recipeBriefingService = mock(RecipeBriefingService.class);


    directExecutor = Runnable::run;

    sut =
        new AsyncRecipeInfoCreationService(
            recipeCaptionService,
            recipeStepService,
            recipeIngredientService,
            recipeTagService,
            recipeDetailMetaService,
            recipeProgressService,
            recipeService,
            recipeDetailService,
            recipeYoutubeMetaService,
            recipeIdentifyService,
            recipeBriefingService,
            directExecutor);
  }

  @Nested
  @DisplayName("성공 플로우")
  class SuccessFlow {

    @Nested
    @DisplayName("Given - 유효한 인자가 주어졌을 때")
    class GivenValidArguments {

      private UUID recipeId;
      private String videoId;
      private URI videoUrl;
      private UUID captionId;
      private RecipeCaption caption;
      private RecipeDetail detail;

      @BeforeEach
      void setUp() {
        recipeId = UUID.randomUUID();
        videoId = "vid-123";
        videoUrl = URI.create("https://youtu.be/vid-123");
        captionId = UUID.randomUUID();
        caption = mock(RecipeCaption.class);
        detail = mock(RecipeDetail.class);

        // RecipeDetail mock 설정
        doReturn(List.of()).when(detail).ingredients();
        doReturn(List.of()).when(detail).tags();
        doReturn(15).when(detail).cookTime();
        doReturn(2).when(detail).servings();
        doReturn("설명").when(detail).description();

        // Service mock 설정
        doReturn(captionId).when(recipeCaptionService).create(videoId, recipeId);
        doReturn(caption).when(recipeCaptionService).find(captionId);
        doReturn(detail).when(recipeDetailService).getRecipeDetails(videoId, caption);
      }

      @Nested
      @DisplayName("When - create 메소드를 호출하면")
      class WhenCreateMethodCalled {

        @Test
        @DisplayName("Then - 올바른 순서로 서비스가 호출되고 성공 처리된다")
        void thenServicesCalledInCorrectOrderAndSuccessProcessed() {
          // When
          sut.create(recipeId, videoId, videoUrl);

          // Then - 순차 구간(READY→CAPTION) 순서 검증
          InOrder inOrder = inOrder(recipeProgressService, recipeCaptionService);
          inOrder
              .verify(recipeProgressService)
              .create(recipeId, RecipeProgressStep.READY, RecipeProgressDetail.READY);
          inOrder.verify(recipeCaptionService).create(videoId, recipeId);
          inOrder
              .verify(recipeProgressService)
              .create(recipeId, RecipeProgressStep.CAPTION, RecipeProgressDetail.CAPTION);
          inOrder.verify(recipeCaptionService).find(captionId);

          // 병렬 구간: 호출 여부 검증
          verify(recipeDetailService).getRecipeDetails(videoId, caption);

          verify(recipeIngredientService).create(eq(recipeId), any());
          verify(recipeProgressService)
              .create(recipeId, RecipeProgressStep.DETAIL, RecipeProgressDetail.INGREDIENT);

          verify(recipeTagService).create(eq(recipeId), any());
          verify(recipeProgressService)
              .create(recipeId, RecipeProgressStep.DETAIL, RecipeProgressDetail.TAG);

          verify(recipeDetailMetaService).create(recipeId, 15, 2, "설명");
          verify(recipeProgressService)
              .create(recipeId, RecipeProgressStep.DETAIL, RecipeProgressDetail.DETAIL_META);

          verify(recipeStepService).create(recipeId, caption);
          verify(recipeProgressService)
              .create(recipeId, RecipeProgressStep.STEP, RecipeProgressDetail.STEP);

          verify(recipeBriefingService).create(videoId, recipeId);
          verify(recipeProgressService)
              .create(recipeId, RecipeProgressStep.BRIEFING, RecipeProgressDetail.BRIEFING);

          // 마무리 처리 검증
          verify(recipeService).success(recipeId);
          verify(recipeProgressService)
              .create(recipeId, RecipeProgressStep.FINISHED, RecipeProgressDetail.FINISHED);

          // Finally 블록 검증
          verify(recipeIdentifyService).delete(videoUrl);

          // YouTube 메타 서비스는 호출되지 않아야 함
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
        private String videoId;
        private URI videoUrl;

        @BeforeEach
        void setUp() {
          recipeId = UUID.randomUUID();
          videoId = "not-cook";
          videoUrl = URI.create("https://youtu.be/not-cook");

          doThrow(new RecipeCaptionException(RecipeCaptionErrorCode.NOT_COOK_RECIPE))
              .when(recipeCaptionService)
              .create(videoId, recipeId);
        }

        @Nested
        @DisplayName("When - create 메소드를 호출하면")
        class WhenCreateMethodCalled {

          @Test
          @DisplayName("Then - 실패 처리되고 ban 처리된다")
          void thenFailedAndBanProcessed() {
            // When
            sut.create(recipeId, videoId, videoUrl);

            // Then
            verify(recipeService).failed(recipeId);
            verify(recipeYoutubeMetaService).ban(recipeId);
            verify(recipeIdentifyService).delete(videoUrl);

            // 성공 관련 메소드들은 호출되지 않아야 함
            verify(recipeService, never()).success(any());
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
        private String videoId;
        private URI videoUrl;

        @BeforeEach
        void setUp() {
          recipeId = UUID.randomUUID();
          videoId = "no-meta";
          videoUrl = URI.create("https://youtu.be/no-meta");

          doThrow(new RecipeCaptionException(RecipeCaptionErrorCode.CAPTION_CREATE_FAIL))
              .when(recipeCaptionService)
              .create(videoId, recipeId);
        }

        @Nested
        @DisplayName("When - create 메소드를 호출하면")
        class WhenCreateMethodCalled {

          @Test
          @DisplayName("Then - 실패 처리만 되고 ban 처리는 되지 않는다")
          void thenOnlyFailedProcessedWithoutBan() {
            // When
            sut.create(recipeId, videoId, videoUrl);

            // Then
            verify(recipeService).failed(recipeId);
            verify(recipeIdentifyService).delete(videoUrl);

            // Ban 처리는 되지 않아야 함
            verify(recipeYoutubeMetaService, never()).ban(any());

            // 성공 관련 메소드들은 호출되지 않아야 함
            verify(recipeService, never()).success(any());
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
        private String videoId;
        private URI videoUrl;
        private UUID captionId;
        private RecipeCaption caption;
        private RecipeDetail detail;

        @BeforeEach
        void setUp() {
          recipeId = UUID.randomUUID();
          videoId = "briefing-fail";
          videoUrl = URI.create("https://youtu.be/briefing-fail");
          captionId = UUID.randomUUID();
          caption = mock(RecipeCaption.class);
          detail = mock(RecipeDetail.class);

          // RecipeDetail mock 설정
          doReturn(List.of()).when(detail).ingredients();
          doReturn(List.of()).when(detail).tags();
          doReturn(15).when(detail).cookTime();
          doReturn(2).when(detail).servings();
          doReturn("설명").when(detail).description();

          // 캡션까지는 성공
          doReturn(captionId).when(recipeCaptionService).create(videoId, recipeId);
          doReturn(caption).when(recipeCaptionService).find(captionId);
          doReturn(detail).when(recipeDetailService).getRecipeDetails(videoId, caption);

          // 브리핑 생성 실패
          doThrow(new RecipeBriefingException(RecipeBriefingErrorCode.BRIEFING_CREATE_FAIL))
              .when(recipeBriefingService)
              .create(videoId, recipeId);
        }

        @Nested
        @DisplayName("When - create 메소드를 호출하면")
        class WhenCreateMethodCalled {

          @Test
          @DisplayName("Then - 실패 처리되고 ban 처리는 되지 않는다")
          void thenFailedProcessedWithoutBan() {
            // When
            sut.create(recipeId, videoId, videoUrl);

            // Then
            verify(recipeService).failed(recipeId);
            verify(recipeIdentifyService).delete(videoUrl);

            // Ban 처리는 되지 않아야 함
            verify(recipeYoutubeMetaService, never()).ban(any());

            // 성공 관련 메소드들은 호출되지 않아야 함
            verify(recipeService, never()).success(any());

            // 캡션과 디테일은 정상 처리되어야 함
            verify(recipeCaptionService).create(videoId, recipeId);
            verify(recipeDetailService).getRecipeDetails(videoId, caption);
            
            // 하지만 브리핑은 실패해야 함
            verify(recipeBriefingService).create(videoId, recipeId);
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
        private String videoId;
        private URI videoUrl;

        @BeforeEach
        void setUp() {
          recipeId = UUID.randomUUID();
          videoId = "boom";
          videoUrl = URI.create("https://youtu.be/boom");

          doThrow(new RuntimeException("boom"))
              .when(recipeCaptionService)
              .create(videoId, recipeId);
        }

        @Nested
        @DisplayName("When - create 메소드를 호출하면")
        class WhenCreateMethodCalled {

          @Test
          @DisplayName("Then - 실패 처리되고 식별자가 삭제된다")
          void thenFailedProcessedAndIdentifierDeleted() {
            // When
            sut.create(recipeId, videoId, videoUrl);

            // Then
            verify(recipeService).failed(recipeId);
            verify(recipeIdentifyService).delete(videoUrl);

            // Ban 및 성공 처리는 되지 않아야 함
            verify(recipeYoutubeMetaService, never()).ban(any());
            verify(recipeService, never()).success(any());

            // 후속 처리는 되지 않아야 함
            verify(recipeDetailService, never()).getRecipeDetails(any(), any());
            verify(recipeStepService, never()).create(any(), any());
          }
        }
      }
    }
  }
}
