package com.cheftory.api.recipeinfo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.List;
import org.springframework.data.domain.Page;

public record SearchedRecipesResponse(
    @JsonProperty("searched_recipes") List<SearchedRecipe> searchedRecipes,
    @JsonProperty("current_page") int currentPage,
    @JsonProperty("total_pages") int totalPages,
    @JsonProperty("total_elements") long totalElements,
    @JsonProperty("has_next") boolean hasNext) {

  public static SearchedRecipesResponse from(Page<RecipeOverview> recipes) {
    List<SearchedRecipe> responses = recipes.stream().map(SearchedRecipe::from).toList();
    return new SearchedRecipesResponse(
        responses,
        recipes.getNumber(),
        recipes.getTotalPages(),
        recipes.getTotalElements(),
        recipes.hasNext());
  }

  private record SearchedRecipe(
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
    private static SearchedRecipe from(RecipeOverview recipe) {
      return new SearchedRecipe(
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
