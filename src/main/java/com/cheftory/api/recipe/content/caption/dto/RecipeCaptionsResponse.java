package com.cheftory.api.recipe.content.caption.dto;

import com.cheftory.api.recipe.content.caption.entity.RecipeCaption;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RecipeCaptionsResponse(
    @JsonProperty("lang_code") String langCode, @JsonProperty("captions") List<Segment> captions) {
  public record Segment(
      @JsonProperty("start") Double start,
      @JsonProperty("end") Double end,
      @JsonProperty("text") String text) {}

  public static RecipeCaptionsResponse from(RecipeCaption caption) {
    List<Segment> segments =
        caption.getSegments().stream()
            .map(segment -> new Segment(segment.getStart(), segment.getEnd(), segment.getText()))
            .toList();

    return new RecipeCaptionsResponse(caption.getLangCode().name(), segments);
  }
}
