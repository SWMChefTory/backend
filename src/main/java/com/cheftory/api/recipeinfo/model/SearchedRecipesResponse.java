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
      @JsonProperty("detail_meta") DetailMeta detailMeta,
      @JsonProperty("video_info") VideoInfo videoInfo,
      @JsonProperty("is_viewed") Boolean isViewed) {

    private record DetailMeta(
        @JsonProperty("description") String description,
        @JsonProperty("servings") Integer servings,
        @JsonProperty("cookingTime") Integer cookingTime) {}

    private record VideoInfo(
        @JsonProperty("video_id") String videoId,
        @JsonProperty("video_title") String title,
        @JsonProperty("video_thumbnail_url") URI thumbnailUrl,
        @JsonProperty("video_seconds") Integer videoSeconds) {}

    private record Tag(@JsonProperty("name") String name) {}

    public static SearchedRecipe from(RecipeOverview recipe) {
      return new SearchedRecipe(
          recipe.getRecipeId().toString(),
          recipe.getVideoTitle(),
          recipe.getTags().stream().map(Tag::new).toList(),
          new DetailMeta(recipe.getDescription(), recipe.getServings(), recipe.getCookTime()),
          new VideoInfo(
              recipe.getVideoId(),
              recipe.getVideoTitle(),
              recipe.getThumbnailUrl(),
              recipe.getVideoSeconds()),
          recipe.getIsViewed());
    }
  }
}
