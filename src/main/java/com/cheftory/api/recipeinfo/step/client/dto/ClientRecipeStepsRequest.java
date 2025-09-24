package com.cheftory.api.recipeinfo.step.client.dto;

import com.cheftory.api.recipeinfo.caption.entity.RecipeCaption;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ClientRecipeStepsRequest(@JsonProperty("captions") List<Caption> recipeCaptions) {
  public record Caption(
      @JsonProperty("start") Double start,
      @JsonProperty("end") Double end,
      @JsonProperty("text") String text) {
    private static Caption from(RecipeCaption.Segment segment) {
      return new Caption(segment.getStart(), segment.getEnd(), segment.getText());
    }
  }

  public static ClientRecipeStepsRequest from(RecipeCaption recipeCaption) {
    return new ClientRecipeStepsRequest(
        recipeCaption.getSegments().stream().map(Caption::from).toList());
  }
}
