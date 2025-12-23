package com.cheftory.api.recipe.creation;

import com.cheftory.api.recipe.content.briefing.RecipeBriefingService;
import com.cheftory.api.recipe.content.caption.RecipeCaptionService;
import com.cheftory.api.recipe.content.caption.entity.RecipeCaption;
import com.cheftory.api.recipe.content.caption.exception.RecipeCaptionErrorCode;
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
import com.cheftory.api.recipe.exception.RecipeException;
import com.cheftory.api.recipe.history.RecipeHistoryService;
import com.cheftory.api.recipe.history.entity.RecipeHistory;
import java.net.URI;
import java.util.List;
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
public class AsyncRecipeCreationService {

  private final RecipeCaptionService recipeCaptionService;
  private final RecipeStepService recipeStepService;
  private final RecipeIngredientService recipeIngredientService;
  private final RecipeTagService recipeTagService;
  private final RecipeDetailMetaService recipeDetailMetaService;
  private final RecipeProgressService recipeProgressService;
  private final RecipeInfoService recipeInfoService;
  private final RecipeDetailService recipeDetailService;
  private final RecipeYoutubeMetaService recipeYoutubeMetaService;
  private final RecipeIdentifyService recipeIdentifyService;
  private final RecipeBriefingService recipeBriefingService;
  private final RecipeHistoryService recipeHistoryService;
  private final AsyncTaskExecutor recipeCreateExecutor;
  private final RecipeCreditPort creditPort;

  @Async("recipeCreateExecutor")
  public void create(UUID recipeId, long creditCost, String videoId, URI videoUrl) {
    boolean shouldBan = false;

    try {
      progressStart(recipeId);

      RecipeCaption caption = progressCaption(recipeId, videoId);

      CompletableFuture<Void> detailFuture =
          CompletableFuture.runAsync(
              () -> progressDetail(recipeId, videoId, caption), recipeCreateExecutor);

      CompletableFuture<Void> stepFuture =
          CompletableFuture.runAsync(() -> progressStep(recipeId, caption), recipeCreateExecutor);

      CompletableFuture<Void> briefingFuture =
          CompletableFuture.runAsync(
              () -> progressBriefing(recipeId, videoId), recipeCreateExecutor);

      CompletableFuture.allOf(detailFuture, stepFuture, briefingFuture).join();

      finalizeRecipe(recipeId);

    } catch (RecipeException e) {
      log.error("레시피 생성 실패: recipeId={}, reason={}", recipeId, e.getErrorMessage(), e);

      if (e.getErrorMessage() == RecipeCaptionErrorCode.NOT_COOK_RECIPE) {
        shouldBan = true;
      }

      failedRecipe(recipeId, creditCost);

    } catch (Exception e) {
      log.error("레시피 생성 실패(Unexpected): recipeId={}", recipeId, e);

      failedRecipe(recipeId, creditCost);

    } finally {
      if (shouldBan) {
        try {
          recipeYoutubeMetaService.ban(recipeId);
        } catch (Exception banEx) {
          log.error("ban 처리 실패: recipeId={}", recipeId, banEx);
        }
      }

      try {
        recipeIdentifyService.delete(videoUrl);
      } catch (Exception identifyEx) {
        log.warn("identify delete 실패: recipeId={}", recipeId, identifyEx);
      }
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
    recipeInfoService.success(recipeId);
    recipeProgressService.create(
        recipeId, RecipeProgressStep.FINISHED, RecipeProgressDetail.FINISHED);
  }

  private void failedRecipe(UUID recipeId, long creditCost) {
    recipeInfoService.failed(recipeId);

    List<RecipeHistory> histories = recipeHistoryService.deleteByRecipe(recipeId);

    histories.forEach(h -> creditPort.refundRecipeCreate(h.getUserId(), recipeId, creditCost));
  }
}
