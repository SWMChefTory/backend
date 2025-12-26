package com.cheftory.api.recipe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;

public record UnCategorizedRecipesResponse(
    @JsonProperty("unCategorized_recipes") List<UnCategorizedRecipe> categorizedRecipes,
    @JsonProperty("current_page") int currentPage,
    @JsonProperty("total_pages") int totalPages,
    @JsonProperty("total_elements") long totalElements,
    @JsonProperty("has_next") boolean hasNext) {
  public static UnCategorizedRecipesResponse from(Page<RecipeHistoryOverview> categorizedRecipes) {
    List<UnCategorizedRecipe> responses =
        categorizedRecipes.stream().map(UnCategorizedRecipe::from).toList();
    return new UnCategorizedRecipesResponse(
        responses,
        categorizedRecipes.getNumber(),
        categorizedRecipes.getTotalPages(),
        categorizedRecipes.getTotalElements(),
        categorizedRecipes.hasNext());
  }

  private record UnCategorizedRecipe(
      @JsonProperty("viewed_at") LocalDateTime viewedAt,
      @JsonProperty("last_play_seconds") Integer lastPlaySeconds,
      @JsonProperty("recipe_id") UUID recipeId,
      @JsonProperty("recipe_title") String recipeTitle,
      @JsonProperty("video_thumbnail_url") URI thumbnailUrl,
      @JsonProperty("video_id") String videoId,
      @JsonProperty("video_seconds") Integer videoSeconds,
      @JsonProperty("description") String description,
      @JsonProperty("cook_time") Integer cookTime,
      @JsonProperty("servings") Integer servings,
      @JsonProperty("created_at") LocalDateTime createdAt,
      @JsonProperty("tags") List<Tag> tags,
      @JsonProperty("credit_cost") Long creditCost) {
    public static UnCategorizedRecipe from(RecipeHistoryOverview info) {
      return new UnCategorizedRecipe(
          info.getViewedAt(),
          info.getLastPlaySeconds(),
          info.getRecipeId(),
          info.getVideoTitle(),
          info.getThumbnailUrl(),
          info.getVideoId(),
          info.getVideoSeconds(),
          info.getDescription(),
          info.getCookTime(),
          info.getServings(),
          info.getRecipeCreatedAt(),
          info.getTags() != null ? info.getTags().stream().map(Tag::from).toList() : null,
          info.getCreditCost());
    }
  }

  private record Tag(@JsonProperty("name") String name) {
    public static Tag from(String tag) {
      return new Tag(tag);
    }
  }
}
