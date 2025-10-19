package com.cheftory.api.recipeinfo.model;

import com.cheftory.api.recipeinfo.tag.RecipeTag;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;

public record RecipeHistoriesResponse(
    @JsonProperty("recipe_histories") List<RecipeHistory> recipeHistories,
    @JsonProperty("current_page") int currentPage,
    @JsonProperty("total_pages") int totalPages,
    @JsonProperty("total_elements") long totalElements,
    @JsonProperty("has_next") boolean hasNext) {
  public static RecipeHistoriesResponse from(Page<RecipeHistoryOverview> recipeHistories) {
    List<RecipeHistory> responses = recipeHistories.stream().map(RecipeHistory::from).toList();
    return new RecipeHistoriesResponse(
        responses,
        recipeHistories.getNumber(),
        recipeHistories.getTotalPages(),
        recipeHistories.getTotalElements(),
        recipeHistories.hasNext());
  }

  private record RecipeHistory(
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
      @JsonProperty("tags") List<Tag> tags) {
    public static RecipeHistory from(RecipeHistoryOverview info) {
      return new RecipeHistory(
          info.getRecipeHistory().getViewedAt(),
          info.getRecipeHistory().getLastPlaySeconds(),
          info.getRecipe().getId(),
          info.getYoutubeMeta().getTitle(),
          info.getYoutubeMeta().getThumbnailUrl(),
          info.getYoutubeMeta().getVideoId(),
          info.getYoutubeMeta().getVideoSeconds(),
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
