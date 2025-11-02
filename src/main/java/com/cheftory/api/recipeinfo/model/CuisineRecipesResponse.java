package com.cheftory.api.recipeinfo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.List;
import org.springframework.data.domain.Page;

public record CuisineRecipesResponse(
    @JsonProperty("cuisine_recipes") List<CuisineRecipe> searchedRecipes,
    @JsonProperty("current_page") int currentPage,
    @JsonProperty("total_pages") int totalPages,
    @JsonProperty("total_elements") long totalElements,
    @JsonProperty("has_next") boolean hasNext) {

  public static CuisineRecipesResponse from(Page<RecipeOverview> recipes) {
    List<CuisineRecipe> responses = recipes.stream().map(CuisineRecipe::from).toList();
    return new CuisineRecipesResponse(
        responses,
        recipes.getNumber(),
        recipes.getTotalPages(),
        recipes.getTotalElements(),
        recipes.hasNext());
  }

  private record CuisineRecipe(
      @JsonProperty("recipe_id") String recipeId,
      @JsonProperty("recipe_title") String recipeTitle,
      @JsonProperty("tags") List<Tag> tags,
      @JsonProperty("is_viewed") Boolean isViewed,
      @JsonProperty("description") String description,
      @JsonProperty("servings") Integer servings,
      @JsonProperty("cooking_time") Integer cookingTime,
      @JsonProperty("video_id") String videoId,
      @JsonProperty("video_thumbnail_url") URI thumbnailUrl,
      @JsonProperty("video_seconds") Integer videoSeconds) {
    private static CuisineRecipe from(RecipeOverview recipe) {
      return new CuisineRecipe(
          recipe.getRecipeId().toString(),
          recipe.getVideoTitle(),
          recipe.getTags().stream().map(Tag::new).toList(),
          recipe.getIsViewed(),
          recipe.getDescription(),
          recipe.getServings(),
          recipe.getCookTime(),
          recipe.getVideoId(),
          recipe.getThumbnailUrl(),
          recipe.getVideoSeconds());
    }

    private record Tag(@JsonProperty("name") String name) {}
  }
}
