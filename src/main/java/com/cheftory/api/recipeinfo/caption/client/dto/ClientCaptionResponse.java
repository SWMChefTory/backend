package com.cheftory.api.recipeinfo.caption.client.dto;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.caption.entity.LangCodeType;
import com.cheftory.api.recipeinfo.caption.entity.RecipeCaption;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record ClientCaptionResponse(
    @JsonProperty("lang_code") @NotNull LangCodeType langCode,
    @JsonProperty("captions") @NotNull List<Segment> segments) {
  private record Segment(
      @JsonProperty("start") Double start,
      @JsonProperty("end") Double end,
      @JsonProperty("text") String text) {}

  public RecipeCaption toRecipeCaption(UUID recipeId, Clock clock) {
    List<RecipeCaption.Segment> recipeSegments =
        segments.stream()
            .map(s -> new RecipeCaption.Segment(s.text(), s.start(), s.end()))
            .toList();

    return RecipeCaption.from(recipeSegments, langCode, recipeId, clock);
  }
}
