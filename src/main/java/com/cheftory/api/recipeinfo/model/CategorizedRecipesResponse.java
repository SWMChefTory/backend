package com.cheftory.api.recipeinfo.model;

import com.cheftory.api.recipeinfo.tag.RecipeTag;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;

public record CategorizedRecipesResponse(
    @JsonProperty("categorized_recipes") List<CategorizedRecipe> categorizedRecipes,
    @JsonProperty("current_page") int currentPage,
    @JsonProperty("total_pages") int totalPages,
    @JsonProperty("total_elements") long totalElements,
    @JsonProperty("has_next") boolean hasNext) {
  public static CategorizedRecipesResponse from(Page<RecipeHistoryOverview> categorizedRecipes) {

    List<CategorizedRecipe> responses =
        categorizedRecipes.stream().map(CategorizedRecipe::from).toList();
    return new CategorizedRecipesResponse(
        responses,
        categorizedRecipes.getNumber(),
        categorizedRecipes.getTotalPages(),
        categorizedRecipes.getTotalElements(),
        categorizedRecipes.hasNext());
  }

  private record CategorizedRecipe(
      @JsonProperty("viewed_at") LocalDateTime viewedAt,
      @JsonProperty("last_play_seconds") Integer lastPlaySeconds,
      @JsonProperty("recipe_id") UUID recipeId,
      @JsonProperty("recipe_title") String recipeTitle,
      @JsonProperty("video_thumbnail_url") URI thumbnailUrl,
      @JsonProperty("video_id") String videoId,
      @JsonProperty("video_seconds") Integer videoSeconds,
      @JsonProperty("category_id") UUID categoryId,
      @JsonProperty("description") String description,
      @JsonProperty("cook_time") Integer cookTime,
      @JsonProperty("servings") Integer servings,
      @JsonProperty("created_at") LocalDateTime createdAt,
      @JsonProperty("tags") List<Tag> tags) {
    public static CategorizedRecipe from(RecipeHistoryOverview info) {
      return new CategorizedRecipe(
          info.getRecipeHistory().getViewedAt(),
          info.getRecipeHistory().getLastPlaySeconds(),
          info.getRecipe().getId(),
          info.getYoutubeMeta().getTitle(),
          info.getYoutubeMeta().getThumbnailUrl(),
          info.getYoutubeMeta().getVideoId(),
          info.getYoutubeMeta().getVideoSeconds(),
          info.getRecipeHistory().getRecipeCategoryId(),
          info.getDetailMeta() != null ? info.getDetailMeta().getDescription() : null,
          info.getDetailMeta() != null ? info.getDetailMeta().getCookTime() : null,
          info.getDetailMeta() != null ? info.getDetailMeta().getServings() : null,
          info.getDetailMeta() != null ? info.getDetailMeta().getCreatedAt() : null,
          info.getTags() != null ? info.getTags().stream().map(Tag::from).toList() : null);
    }
  }

  private record Tag(@JsonProperty("name") String name) {
    public static Tag from(RecipeTag tag) {
      return new Tag(tag.getTag());
    }
  }
}
