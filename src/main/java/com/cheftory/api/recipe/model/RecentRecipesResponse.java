package com.cheftory.api.recipe.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RecentRecipesResponse(
    @JsonProperty("recent_recipes")
    List<RecentRecipeResponse> recentRecipes
) {
  public static RecentRecipesResponse from(List<RecipeHistoryOverview> recentRecipes) {
    List<RecentRecipeResponse> responses = recentRecipes.stream()
        .map(RecentRecipeResponse::from)
        .toList();
    return new RecentRecipesResponse(responses);
  }

  public record RecentRecipeResponse(
      @JsonProperty("viewed_at")
      LocalDateTime viewedAt,

      @JsonProperty("last_play_seconds")
      Integer lastPlaySeconds,

      @JsonProperty("recipe_id")
      UUID recipeId,

      @JsonProperty("recipe_title")
      String recipeTitle,

      @JsonProperty("video_thumbnail_url")
      URI thumbnailUrl,

      @JsonProperty("video_id")
      String videoId,

      @JsonProperty("video_seconds")
      Integer videoSeconds
  ) {
    public static RecentRecipeResponse from(RecipeHistoryOverview info) {
      return new RecentRecipeResponse(
          info.getRecipeViewStatusInfo().getViewedAt(),
          info.getRecipeViewStatusInfo().getLastPlaySeconds(),
          info.getRecipeOverview().getId(),
          info.getRecipeOverview().getVideoInfo().getTitle(),
          info.getRecipeOverview().getVideoInfo().getThumbnailUrl(),
          info.getRecipeOverview().getVideoInfo().getVideoId(),
          info.getRecipeOverview().getVideoInfo().getVideoSeconds()
      );
    }
  }
}