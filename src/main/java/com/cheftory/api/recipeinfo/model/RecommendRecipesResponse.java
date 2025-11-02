package com.cheftory.api.recipeinfo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
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
      @JsonProperty("tags") List<Tag> tags,
      @JsonProperty("is_viewed") Boolean isViewed,
      @JsonProperty("description") String description,
      @JsonProperty("servings") Integer servings,
      @JsonProperty("cooking_time") Integer cookingTime,
      @JsonProperty("video_id") String videoId,
      @JsonProperty("video_title") String title,
      @JsonProperty("video_thumbnail_url") URI thumbnailUrl,
      @JsonProperty("video_seconds") Integer videoSeconds) {
    public static RecommendRecipe from(RecipeOverview recipe) {
      return new RecommendRecipe(
          recipe.getRecipeId().toString(),
          recipe.getVideoTitle(),
          recipe.getTags().stream().map(Tag::new).toList(),
          recipe.getIsViewed(),
          recipe.getDescription(),
          recipe.getServings(),
          recipe.getCookTime(),
          recipe.getVideoId(),
          recipe.getVideoTitle(),
          recipe.getThumbnailUrl(),
          recipe.getVideoSeconds());
    }

    private record Tag(@JsonProperty("name") String name) {}

  }
}
