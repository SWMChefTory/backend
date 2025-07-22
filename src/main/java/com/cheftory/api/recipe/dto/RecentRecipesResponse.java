package com.cheftory.api.recipe.dto;

import com.cheftory.api.recipe.model.RecentRecipeOverview;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access=AccessLevel.PRIVATE)
@Getter
public class RecentRecipesResponse {

  @JsonProperty("recent_recipes")
  private List<RecentRecipeResponse> viewStateInfos;

  public static RecentRecipesResponse from(List<RecentRecipeOverview> recentRecipes) {
    List<RecentRecipeResponse> infoResponses = recentRecipes.stream()
        .map(RecentRecipeResponse::from)
        .toList();
    return RecentRecipesResponse.builder()
        .viewStateInfos(infoResponses)
        .build();
  }

  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  @Builder(access = AccessLevel.PRIVATE)
  @Getter
  public static class RecentRecipeResponse {
  @JsonProperty("viewed_at")
  private LocalDateTime viewedAt;

  @JsonProperty("last_play_seconds")
  private Integer lastPlaySeconds;

  @JsonProperty("recipe_id")
  private UUID recipeId;

  @JsonProperty("recipe_title")
  private String recipeTitle;

  @JsonProperty("video_thumbnail_url")
  private URI thumbnailUrl;

  @JsonProperty("video_id")
  private String videoId;

  public static RecentRecipeResponse from(RecentRecipeOverview info) {
    return new RecentRecipeResponse(
        info.getRecipeViewStatusInfo().getViewedAt(),
        info.getRecipeViewStatusInfo().getLastPlaySeconds(),
        info.getRecipeOverview().getId(),
        info.getRecipeOverview().getVideoInfo().getTitle(),
        info.getRecipeOverview().getVideoInfo().getThumbnailUrl(),
        info.getRecipeOverview().getVideoInfo().getVideoId()
    );
  }
}
}
