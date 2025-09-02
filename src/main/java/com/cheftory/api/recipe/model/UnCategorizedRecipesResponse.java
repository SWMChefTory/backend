package com.cheftory.api.recipe.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;

public record UnCategorizedRecipesResponse(
    @JsonProperty("unCategorized_recipes")
    List<UnCategorizedRecipeResponse> categorizedRecipes,

    @JsonProperty("current_page")
    int currentPage,

    @JsonProperty("total_pages")
    int totalPages,

    @JsonProperty("total_elements")
    long totalElements,

    @JsonProperty("has_next")
    boolean hasNext
) {
  public static UnCategorizedRecipesResponse from(Page<RecipeHistoryOverview> categorizedRecipes) {
    List<UnCategorizedRecipeResponse> responses = categorizedRecipes.stream()
        .map(UnCategorizedRecipeResponse::from)
        .toList();
    return new UnCategorizedRecipesResponse(
        responses,
        categorizedRecipes.getNumber(),
        categorizedRecipes.getTotalPages(),
        categorizedRecipes.getTotalElements(),
        categorizedRecipes.hasNext()
    );
  }

  public record UnCategorizedRecipeResponse(
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
  ){
    public static UnCategorizedRecipeResponse from(RecipeHistoryOverview info) {
      return new UnCategorizedRecipeResponse(
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
