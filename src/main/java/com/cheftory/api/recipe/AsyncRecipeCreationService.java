package com.cheftory.api.recipe;

import com.cheftory.api.recipe.caption.RecipeCaptionService;
import com.cheftory.api.recipe.analysis.entity.RecipeAnalysis;
import com.cheftory.api.recipe.caption.entity.RecipeCaption;
import com.cheftory.api.recipe.entity.Recipe;
import com.cheftory.api.recipe.entity.RecipeStatus;
import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
import com.cheftory.api.recipe.analysis.RecipeAnalysisService;
import com.cheftory.api.recipe.step.RecipeStepService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class AsyncRecipeCreationService {

  private final RecipeRepository recipeRepository;
  private final RecipeCaptionService recipeCaptionService;
  private final RecipeAnalysisService recipeAnalysisService;
  private final RecipeStepService recipeStepService;

  @Async
  public void create(UUID recipeId) {
    try {
      Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(()->new RecipeException(
          RecipeErrorCode.RECIPE_NOT_FOUND));
      String videoId = recipe.getVideoInfo().getVideoId();
      RecipeCaption recipeCaption = handleCaption(videoId,recipeId);
      handleAnalysis(recipeId, videoId, recipeCaption);
      handleSteps(recipeId, recipeCaption);
      recipeRepository.updateStatus(recipeId, RecipeStatus.COMPLETED);
    } catch (Exception e) {
      log.error("레시피 생성에 실패했습니다.", e);
      recipeRepository.updateStatus(recipeId, RecipeStatus.FAILED);
    }
  }

  private RecipeCaption handleCaption(String videoId, UUID recipeId) {
    UUID captionId = recipeCaptionService.create(videoId, recipeId);
    return recipeCaptionService.find(captionId);
  }

  private void handleAnalysis(UUID recipeId, String videoId, RecipeCaption recipeCaption) {
    recipeAnalysisService.create(recipeId, videoId, recipeCaption);
  }

  private void handleSteps(UUID recipeId, RecipeCaption recipeCaption) {
    recipeStepService.create(recipeId, recipeCaption);
  }
}
