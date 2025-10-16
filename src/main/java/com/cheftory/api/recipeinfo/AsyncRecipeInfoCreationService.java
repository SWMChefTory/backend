package com.cheftory.api.recipeinfo;

import com.cheftory.api.recipeinfo.briefing.RecipeBriefingService;
import com.cheftory.api.recipeinfo.briefing.exception.RecipeBriefingErrorCode;
import com.cheftory.api.recipeinfo.caption.RecipeCaptionService;
import com.cheftory.api.recipeinfo.caption.entity.RecipeCaption;
import com.cheftory.api.recipeinfo.caption.exception.RecipeCaptionErrorCode;
import com.cheftory.api.recipeinfo.detail.RecipeDetail;
import com.cheftory.api.recipeinfo.detail.RecipeDetailService;
import com.cheftory.api.recipeinfo.detailMeta.RecipeDetailMetaService;
import com.cheftory.api.recipeinfo.exception.RecipeInfoException;
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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class AsyncRecipeInfoCreationService {

  private final RecipeCaptionService recipeCaptionService;
  private final RecipeStepService recipeStepService;
  private final RecipeIngredientService recipeIngredientService;
  private final RecipeTagService recipeTagService;
  private final RecipeDetailMetaService recipeDetailMetaService;
  private final RecipeProgressService recipeProgressService;
  private final RecipeService recipeService;
  private final RecipeDetailService recipeDetailService;
  private final RecipeYoutubeMetaService recipeYoutubeMetaService;
  private final RecipeIdentifyService recipeIdentifyService;
  private final RecipeBriefingService recipeBriefingService;
  private final AsyncTaskExecutor recipeInfoCreateExecutor;

  @Async("recipeInfoCreateExecutor")
  public void create(UUID recipeId, String videoId, URI videoUrl) {
    try {
      progressStart(recipeId);
      RecipeCaption caption = progressCaption(recipeId, videoId);
      CompletableFuture<Void> detailFuture =
          CompletableFuture.runAsync(
              () -> {
                progressDetail(recipeId, videoId, caption);
              },
              recipeInfoCreateExecutor);

      CompletableFuture<Void> stepFuture =
          CompletableFuture.runAsync(
              () -> {
                progressStep(recipeId, caption);
              },
              recipeInfoCreateExecutor);

      CompletableFuture<Void> briefingFuture =
          CompletableFuture.runAsync(
              () -> {
                progressBriefing(recipeId, videoId);
              },
              recipeInfoCreateExecutor);

      CompletableFuture.allOf(detailFuture, stepFuture, briefingFuture).join();

      finalizeRecipe(recipeId);
    } catch (RecipeInfoException e) {
      if (e.getErrorMessage() == RecipeCaptionErrorCode.NOT_COOK_RECIPE) {
        log.error("레시피 생성에 실패했습니다. - 차단된 영상", e);
        failedRecipe(recipeId);
        recipeYoutubeMetaService.ban(recipeId);
      }
      if (e.getErrorMessage() == RecipeCaptionErrorCode.CAPTION_CREATE_FAIL) {
        log.error("레시피 생성에 실패했습니다. - 영상 정보 없음", e);
        failedRecipe(recipeId);
      }
      if (e.getErrorMessage() == RecipeBriefingErrorCode.BRIEFING_CREATE_FAIL) {
        log.error("레시피 생성에 실패했습니다. - 브리핑 생성 실패", e);
        failedRecipe(recipeId);
      }
    } catch (Exception e) {
      log.error("레시피 생성에 실패했습니다.", e);
      failedRecipe(recipeId);
    } finally {
      recipeIdentifyService.delete(videoUrl);
    }
  }

  private void progressStart(UUID recipeId) {
    recipeProgressService.create(recipeId, RecipeProgressStep.READY, RecipeProgressDetail.READY);
  }

  private void progressStep(UUID recipeId, RecipeCaption caption) {
    recipeStepService.create(recipeId, caption);
    recipeProgressService.create(recipeId, RecipeProgressStep.STEP, RecipeProgressDetail.STEP);
  }

  private void progressDetail(UUID recipeId, String videoId, RecipeCaption caption) {
    RecipeDetail detail = recipeDetailService.getRecipeDetails(videoId, caption);
    recipeIngredientService.create(recipeId, detail.ingredients());
    recipeProgressService.create(
        recipeId, RecipeProgressStep.DETAIL, RecipeProgressDetail.INGREDIENT);
    recipeTagService.create(recipeId, detail.tags());
    recipeProgressService.create(recipeId, RecipeProgressStep.DETAIL, RecipeProgressDetail.TAG);
    recipeDetailMetaService.create(
        recipeId, detail.cookTime(), detail.servings(), detail.description());
    recipeProgressService.create(
        recipeId, RecipeProgressStep.DETAIL, RecipeProgressDetail.DETAIL_META);
  }

  private RecipeCaption progressCaption(UUID recipeId, String videoId) {
    UUID captionId = recipeCaptionService.create(videoId, recipeId);
    recipeProgressService.create(
        recipeId, RecipeProgressStep.CAPTION, RecipeProgressDetail.CAPTION);
    return recipeCaptionService.get(captionId);
  }

  private void progressBriefing(UUID recipeId, String videoId) {
    recipeBriefingService.create(videoId, recipeId);
    recipeProgressService.create(
        recipeId, RecipeProgressStep.BRIEFING, RecipeProgressDetail.BRIEFING);
  }

  private void finalizeRecipe(UUID recipeId) {
    recipeService.success(recipeId);
    recipeProgressService.create(
        recipeId, RecipeProgressStep.FINISHED, RecipeProgressDetail.FINISHED);
  }

  private void failedRecipe(UUID recipeId) {
    recipeService.failed(recipeId);
  }
}
