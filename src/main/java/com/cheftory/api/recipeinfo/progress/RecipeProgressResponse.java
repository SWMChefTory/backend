package com.cheftory.api.recipeinfo.progress;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RecipeProgressResponse(
    @JsonProperty("recipe_progress_statuses") List<ProgressStatus> progressStatuses) {

  public record ProgressStatus(
      @JsonProperty("progress_detail") RecipeProgressDetail progressDetail,
      @JsonProperty("progress_step") RecipeProgressStep progressStep) {
    public static ProgressStatus of(
        RecipeProgressDetail recipeProgressDetail, RecipeProgressStep currentStep) {
      return new ProgressStatus(recipeProgressDetail, currentStep);
    }
  }

  public static RecipeProgressResponse of(List<RecipeProgress> recipeProgresses) {
    List<ProgressStatus> progressStatuses =
        recipeProgresses.stream()
            .map(progress -> ProgressStatus.of(progress.getDetail(), progress.getStep()))
            .toList();

    return new RecipeProgressResponse(progressStatuses);
  }
}
