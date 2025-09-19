package com.cheftory.api.recipeinfo.caption.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ClientCaptionRequest {
  @JsonProperty("video_id")
  private String videoId;

  public static ClientCaptionRequest from(String videoId) {
    return ClientCaptionRequest.builder().videoId(videoId).build();
  }
}
