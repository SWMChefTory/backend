package com.cheftory.api.recipe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import lombok.Getter;

@Getter
public class RecipeCreateRequest {
  @JsonProperty("video_url")
  private URI videoUrl;
}
