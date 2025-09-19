package com.cheftory.api.recipeinfo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;

public record UnCategorizedRecipesResponse(
    @JsonProperty("unCategorized_recipes") List<UnCategorizedRecipe> categorizedRecipes,
    @JsonProperty("current_page") int currentPage,
    @JsonProperty("total_pages") int totalPages,
    @JsonProperty("total_elements") long totalElements,
    @JsonProperty("has_next") boolean hasNext) {
  public static UnCategorizedRecipesResponse from(Page<RecipeHistory> categorizedRecipes) {
    List<UnCategorizedRecipe> responses =
        categorizedRecipes.stream().map(UnCategorizedRecipe::from).toList();
    return new UnCategorizedRecipesResponse(
        responses,
        categorizedRecipes.getNumber(),
        categorizedRecipes.getTotalPages(),
        categorizedRecipes.getTotalElements(),
        categorizedRecipes.hasNext());
  }

  private record UnCategorizedRecipe(
      @JsonProperty("viewed_at") LocalDateTime viewedAt,
      @JsonProperty("last_play_seconds") Integer lastPlaySeconds,
      @JsonProperty("recipe_id") UUID recipeId,
      @JsonProperty("recipe_title") String recipeTitle,
      @JsonProperty("video_thumbnail_url") URI thumbnailUrl,
      @JsonProperty("video_id") String videoId,
      @JsonProperty("video_seconds") Integer videoSeconds) {
    public static UnCategorizedRecipe from(RecipeHistory info) {
      return new UnCategorizedRecipe(
          info.getRecipeViewStatus().getViewedAt(),
          info.getRecipeViewStatus().getLastPlaySeconds(),
          info.getRecipe().getId(),
          info.getYoutubeMeta().getTitle(),
          info.getYoutubeMeta().getThumbnailUrl(),
          info.getYoutubeMeta().getVideoId(),
          info.getYoutubeMeta().getVideoSeconds());
    }
  }
}
