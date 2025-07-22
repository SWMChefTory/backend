package com.cheftory.api.recipe.service;

import com.cheftory.api.recipe.caption.RecipeCaptionService;
import com.cheftory.api.recipe.caption.dto.CaptionInfo;
import com.cheftory.api.recipe.entity.Recipe;
import com.cheftory.api.recipe.entity.RecipeStatus;
import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
import com.cheftory.api.recipe.repository.RecipeRepository;
import com.cheftory.api.recipe.ingredients.RecipeIngredientsService;
import com.cheftory.api.recipe.ingredients.entity.Ingredient;
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
  private final RecipeIngredientsService recipeIngredientsService;
  private final RecipeStepService recipeStepService;


  //어짜피 이거는 RecipeService에서만 쓰고 있는 recipeId만 보내줄건데 recipeId가 있는지 체크할 필요가 있을까?
  //중간에 AI 서버에서 실패했을 때 대응하는 방법이 필요해 보임.
  @Async
  public void create(UUID recipeId) {
    try {
      Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(()->new RecipeException(
          RecipeErrorCode.RECIPE_NOT_FOUND));
      String videoId = recipe.getVideoInfo().getVideoId();
      CaptionInfo captionInfo = handleCaption(videoId,recipeId);
      List<Ingredient> ingredientsContent = handleIngredients(recipeId, videoId, captionInfo);
      handleSteps(videoId, recipeId, captionInfo, ingredientsContent);
      recipeRepository.updateStatus(recipeId, RecipeStatus.COMPLETED);
    } catch (Exception e) {
      log.error("레시피 생성에 실패했습니다.");
      recipeRepository.updateStatus(recipeId, RecipeStatus.FAILED);
    }
  }

  private CaptionInfo handleCaption(String videoId, UUID recipeId) {
    UUID captionId = recipeCaptionService.create(videoId, recipeId);
    return recipeCaptionService.findCaptionInfo(captionId);
  }

  private List<Ingredient> handleIngredients(UUID recipeId, String videoId, CaptionInfo captionInfo) {
    UUID ingredientsId = recipeIngredientsService.create(recipeId, videoId, captionInfo);
    return recipeIngredientsService.findIngredientsInfo(ingredientsId).getIngredients();
  }

  private void handleSteps(String videoId, UUID recipeId, CaptionInfo captionInfo, List<Ingredient> ingredients) {
    recipeStepService.create(videoId, recipeId, captionInfo, ingredients);
  }
}
