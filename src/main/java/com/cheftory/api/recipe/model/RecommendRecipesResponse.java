package com.cheftory.api.recipe.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RecommendRecipesResponse(
    @JsonProperty("recommend_recipes")
    List<RecommendRecipeResponse> recommendRecipes
) {

  public static RecommendRecipesResponse from(List<RecipeOverview> recipes) {
    List<RecommendRecipeResponse> responses = recipes.stream()
        .map(RecommendRecipeResponse::from)
        .toList();
    return new RecommendRecipesResponse(responses);
  }

  public record RecommendRecipeResponse(
      @JsonProperty("recipe_id")
      String recipeId,
      @JsonProperty("recipe_title")
      String recipeTitle,
      @JsonProperty("video_thumbnail_url")
      String videoThumbnailUrl,
      @JsonProperty("video_id")
      String videoId,
      @JsonProperty("count")
      Integer count,
      @JsonProperty("video_url")
      String videoUrl
  ) {
    public static RecommendRecipeResponse from(RecipeOverview recipe) {
      return new RecommendRecipeResponse(
          recipe.getId().toString(),
          recipe.getVideoInfo().getTitle(),
          recipe.getVideoInfo().getThumbnailUrl().toString(),
          recipe.getVideoInfo().getVideoId(),
          recipe.getCount(),
          recipe.getVideoInfo().getVideoUri().toString()
      );
    }
  }
}
