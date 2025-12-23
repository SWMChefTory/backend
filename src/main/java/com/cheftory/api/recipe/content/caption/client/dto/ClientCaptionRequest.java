package com.cheftory.api.recipe.content.caption.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ClientCaptionRequest {
  @JsonProperty("video_id")
  private String videoId;

  public static ClientCaptionRequest from(String videoId) {
    return new ClientCaptionRequest(videoId);
  }
}
