package com.cheftory.api.recipeinfo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.springframework.data.domain.Page;

public record TrendRecipesResponse(
    @JsonProperty("trend_recipes") List<TrendRecipeResponse> recommendRecipes,
    @JsonProperty("current_page") int currentPage,
    @JsonProperty("total_pages") int totalPages,
    @JsonProperty("total_elements") long totalElements,
    @JsonProperty("has_next") boolean hasNext) {

  public static TrendRecipesResponse from(Page<RecipeOverview> recipes) {
    List<TrendRecipeResponse> responses = recipes.stream().map(TrendRecipeResponse::from).toList();
    return new TrendRecipesResponse(
        responses,
        recipes.getNumber(),
        recipes.getTotalPages(),
        recipes.getTotalElements(),
        recipes.hasNext());
  }

  private record TrendRecipeResponse(
      @JsonProperty("recipe_id") String recipeId,
      @JsonProperty("recipe_title") String recipeTitle,
      @JsonProperty("video_thumbnail_url") String videoThumbnailUrl,
      @JsonProperty("video_id") String videoId,
      @JsonProperty("count") Integer count,
      @JsonProperty("video_url") String videoUrl,
      @JsonProperty("is_viewed") Boolean isViewed,
      @JsonProperty("video_type") String videoType) {
    public static TrendRecipeResponse from(RecipeOverview recipe) {
      return new TrendRecipeResponse(
          recipe.getRecipeId().toString(),
          recipe.getVideoTitle(),
          recipe.getThumbnailUrl().toString(),
          recipe.getVideoId(),
          recipe.getViewCount(),
          recipe.getVideoUri().toString(),
          recipe.getIsViewed(),
          recipe.getVideoType().name());
    }
  }
}
