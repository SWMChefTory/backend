package com.cheftory.api.recipeinfo.model;

import com.cheftory.api.recipeinfo.detailMeta.RecipeDetailMeta;
import com.cheftory.api.recipeinfo.tag.RecipeTag;
import com.cheftory.api.recipeinfo.youtubemeta.RecipeYoutubeMeta;
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
      @JsonProperty("video_info") VideoInfo videoInfo) {

    private record DetailMeta(
        @JsonProperty("description") String description,
        @JsonProperty("servings") Integer servings,
        @JsonProperty("cookingTime") Integer cookingTime) {

      public static DetailMeta from(RecipeDetailMeta detailMeta) {
        return new DetailMeta(
            detailMeta.getDescription(), detailMeta.getServings(), detailMeta.getCookTime());
      }
    }

    private record VideoInfo(
        @JsonProperty("video_id") String videoId,
        @JsonProperty("video_title") String title,
        @JsonProperty("video_thumbnail_url") URI thumbnailUrl,
        @JsonProperty("video_seconds") Integer videoSeconds) {
      public static VideoInfo from(RecipeYoutubeMeta youtubeMeta) {
        return new VideoInfo(
            youtubeMeta.getVideoId(),
            youtubeMeta.getTitle(),
            youtubeMeta.getThumbnailUrl(),
            youtubeMeta.getVideoSeconds());
      }
    }

    private record Tag(@JsonProperty("name") String name) {
      public static Tag from(RecipeTag tag) {
        return new Tag(tag.getTag());
      }
    }

    public static SearchedRecipe from(RecipeOverview recipe) {
      return new SearchedRecipe(
          recipe.getRecipe().getId().toString(),
          recipe.getYoutubeMeta().getTitle(),
          recipe.getTags().stream().map(Tag::from).toList(),
          DetailMeta.from(recipe.getDetailMeta()),
          VideoInfo.from(recipe.getYoutubeMeta()));
    }
  }
}
