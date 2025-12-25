package com.cheftory.api.recipe.dto;

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
  public static RecentRecipesResponse from(Page<RecipeHistoryOverview> recentRecipes) {
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
      @JsonProperty("description") String description,
      @JsonProperty("cook_time") Integer cookTime,
      @JsonProperty("servings") Integer servings,
      @JsonProperty("created_at") LocalDateTime createdAt,
      @JsonProperty("video_seconds") Integer videoSeconds,
      @JsonProperty("tags") List<Tag> tags,
      @JsonProperty("recipe_status") String recipeStatus,
      @JsonProperty("credit_cost") Long creditCost) {
    public static RecentRecipeResponse from(RecipeHistoryOverview info) {
      return new RecentRecipeResponse(
          info.getViewedAt(),
          info.getLastPlaySeconds(),
          info.getRecipeId(),
          info.getVideoTitle(),
          info.getThumbnailUrl(),
          info.getVideoId(),
          info.getDescription(),
          info.getCookTime(),
          info.getServings(),
          info.getRecipeCreatedAt(),
          info.getVideoSeconds(),
          info.getTags() != null ? info.getTags().stream().map(Tag::from).toList() : null,
          info.getRecipeStatus().name(),
          info.getCreditCost());
    }

    private record Tag(@JsonProperty("name") String name) {
      public static Tag from(String tag) {
        return new Tag(tag);
      }
    }
  }
}
