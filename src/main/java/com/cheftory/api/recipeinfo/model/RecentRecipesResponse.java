package com.cheftory.api.recipeinfo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;

public record RecentRecipesResponse(
    @JsonProperty("recent_recipes") List<RecentRecipeResponse> recentRecipes,
    @JsonProperty("current_page") int currentPage,
    @JsonProperty("total_pages") int totalPages,
    @JsonProperty("total_elements") long totalElements,
    @JsonProperty("has_next") boolean hasNext) {
  public static RecentRecipesResponse from(Page<RecipeHistory> recentRecipes) {
    List<RecentRecipeResponse> responses =
        recentRecipes.stream().map(RecentRecipeResponse::from).toList();
    return new RecentRecipesResponse(
        responses,
        recentRecipes.getNumber(),
        recentRecipes.getTotalPages(),
        recentRecipes.getTotalElements(),
        recentRecipes.hasNext());
  }

  public record RecentRecipeResponse(
      @JsonProperty("viewed_at") LocalDateTime viewedAt,
      @JsonProperty("last_play_seconds") Integer lastPlaySeconds,
      @JsonProperty("recipe_id") UUID recipeId,
      @JsonProperty("recipe_title") String recipeTitle,
      @JsonProperty("video_thumbnail_url") URI thumbnailUrl,
      @JsonProperty("video_id") String videoId,
      @JsonProperty("video_seconds") Integer videoSeconds,
      @JsonProperty("recipe_status") String recipeStatus) {
    public static RecentRecipeResponse from(RecipeHistory info) {
      return new RecentRecipeResponse(
          info.getRecipeViewStatus().getViewedAt(),
          info.getRecipeViewStatus().getLastPlaySeconds(),
          info.getRecipe().getId(),
          info.getYoutubeMeta().getTitle(),
          info.getYoutubeMeta().getThumbnailUrl(),
          info.getYoutubeMeta().getVideoId(),
          info.getYoutubeMeta().getVideoSeconds(),
          info.getRecipe().getRecipeStatus().name());
    }
  }
}
