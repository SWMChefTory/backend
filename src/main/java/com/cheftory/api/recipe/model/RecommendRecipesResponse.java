package com.cheftory.api.recipe.model;

import com.cheftory.api.recipe.entity.Recipe;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.springframework.data.domain.Page;

public record RecommendRecipesResponse(
    @JsonProperty("recommend_recipes")
    List<RecommendRecipe> recommendRecipes,

    @JsonProperty("current_page")
    int currentPage,

    @JsonProperty("total_pages")
    int totalPages,

    @JsonProperty("total_elements")
    long totalElements,

    @JsonProperty("has_next")
    boolean hasNext
) {

  public static RecommendRecipesResponse from(Page<Recipe> recipes) {
    List<RecommendRecipe> responses = recipes.stream()
        .map(RecommendRecipe::from)
        .toList();
    return new RecommendRecipesResponse(
        responses,
        recipes.getNumber(),
        recipes.getTotalPages(),
        recipes.getTotalElements(),
        recipes.hasNext());
  }

  private record RecommendRecipe(
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
    public static RecommendRecipe from(Recipe recipe) {
      return new RecommendRecipe(
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
