package com.cheftory.api.recipeinfo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.UUID;

public record RecipeCreateRequest(@JsonProperty("video_url") URI videoUrl) {
  public RecipeCreationTarget toCrawlerTarget() {
    return new RecipeCreationTarget.Crawler(videoUrl);
  }

  public RecipeCreationTarget toUserTarget(UUID userId) {
    return new RecipeCreationTarget.User(videoUrl, userId);
  }
}
