package com.cheftory.api.recipeinfo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.springframework.data.domain.Page;

public record RecommendRecipesResponse(
    @JsonProperty("recommend_recipes") List<RecommendRecipe> recommendRecipes,
    @JsonProperty("current_page") int currentPage,
    @JsonProperty("total_pages") int totalPages,
    @JsonProperty("total_elements") long totalElements,
    @JsonProperty("has_next") boolean hasNext) {

  public static RecommendRecipesResponse from(Page<RecipeOverview> recipes) {
    List<RecommendRecipe> responses = recipes.stream().map(RecommendRecipe::from).toList();
    return new RecommendRecipesResponse(
        responses,
        recipes.getNumber(),
        recipes.getTotalPages(),
        recipes.getTotalElements(),
        recipes.hasNext());
  }

  private record RecommendRecipe(
      @JsonProperty("recipe_id") String recipeId,
      @JsonProperty("recipe_title") String recipeTitle,
      @JsonProperty("video_thumbnail_url") String videoThumbnailUrl,
      @JsonProperty("video_id") String videoId,
      @JsonProperty("count") Integer count,
      @JsonProperty("video_url") String videoUrl) {
    public static RecommendRecipe from(RecipeOverview recipe) {
      return new RecommendRecipe(
          recipe.getRecipe().getId().toString(),
          recipe.getYoutubeMeta().getTitle(),
          recipe.getYoutubeMeta().getThumbnailUrl().toString(),
          recipe.getYoutubeMeta().getVideoId(),
          recipe.getRecipe().getViewCount(),
          recipe.getYoutubeMeta().getVideoUri().toString());
    }
  }
}
