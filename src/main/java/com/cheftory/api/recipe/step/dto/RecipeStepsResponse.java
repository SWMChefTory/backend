package com.cheftory.api.recipe.step.dto;

import com.cheftory.api.recipe.step.entity.RecipeStep;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public record RecipeStepsResponse(
    @JsonProperty("steps")
    List<RecipeStepResponse> steps
) {
  public static RecipeStepsResponse from(List<RecipeStepInfo> recipeSteps) {
    List<RecipeStepResponse> stepResponses = recipeSteps.stream()
        .map(RecipeStepResponse::from)
        .toList();
    return new RecipeStepsResponse(stepResponses);
  }

  public record RecipeStepResponse(
      @JsonProperty("id")
      UUID id,
      @JsonProperty("step_order")
      Integer stepOrder,
      @JsonProperty("subtitle")
      String subtitle,
      @JsonProperty("details")
      List<String> details,
      @JsonProperty("start")
      Double start,
      @JsonProperty("end")
      Double end
  ) {
    public static RecipeStepResponse from(RecipeStepInfo step) {
      return new RecipeStepResponse(
          step.getId(),
          step.getStepOrder(),
          step.getSubtitle(),
          step.getDetails(),
          step.getStart(),
          step.getEnd()
      );
    }
  }
}