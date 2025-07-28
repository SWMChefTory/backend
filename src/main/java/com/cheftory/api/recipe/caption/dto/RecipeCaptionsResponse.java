package com.cheftory.api.recipe.caption.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RecipeCaptionsResponse(
  @JsonProperty("lang_code")
  String langCode,
  @JsonProperty("captions")
  Segment[] captions
) {
  public record Segment(
      @JsonProperty("start")
      Double start,
      @JsonProperty("end")
      Double end,
      @JsonProperty("text")
      String text
  ) {}

  public static RecipeCaptionsResponse from(CaptionInfo captionInfo) {
    Segment[] segments = captionInfo.getCaptions().stream()
        .map(segment -> new Segment(segment.getStart(), segment.getEnd(), segment.getText()))
        .toArray(Segment[]::new);

    return new RecipeCaptionsResponse(captionInfo.getLangCodeType().name(), segments);
  }
}
