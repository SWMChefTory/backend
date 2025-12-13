package com.cheftory.api.recipeinfo.dto;

import com.cheftory.api.recipeinfo.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipeinfo.progress.entity.RecipeProgressStep;
import com.cheftory.api.recipeinfo.recipe.entity.RecipeStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RecipeProgressResponse(
    @JsonProperty("recipe_progress_statuses") List<ProgressStatus> progressStatuses,
    @JsonProperty("recipe_status") RecipeStatus recipeStatus) {

  public record ProgressStatus(
      @JsonProperty("progress_detail") RecipeProgressDetail progressDetail,
      @JsonProperty("progress_step") RecipeProgressStep progressStep) {
    public static ProgressStatus of(
        RecipeProgressDetail recipeProgressDetail, RecipeProgressStep currentStep) {
      return new ProgressStatus(recipeProgressDetail, currentStep);
    }
  }

  public static RecipeProgressResponse of(RecipeProgressStatus recipeProgressStatus) {
    List<ProgressStatus> progressStatuses =
        recipeProgressStatus.getProgresses().stream()
            .map(progress -> ProgressStatus.of(progress.getDetail(), progress.getStep()))
            .toList();

    return new RecipeProgressResponse(
        progressStatuses, recipeProgressStatus.getRecipe().getRecipeStatus());
  }
}
