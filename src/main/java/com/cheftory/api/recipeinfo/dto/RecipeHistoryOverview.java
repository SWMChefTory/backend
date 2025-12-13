package com.cheftory.api.recipeinfo.model;

import com.cheftory.api.recipeinfo.detailMeta.RecipeDetailMeta;
import com.cheftory.api.recipeinfo.history.RecipeHistory;
import com.cheftory.api.recipeinfo.recipe.entity.Recipe;
import com.cheftory.api.recipeinfo.recipe.entity.RecipeStatus;
import com.cheftory.api.recipeinfo.tag.RecipeTag;
import com.cheftory.api.recipeinfo.youtubemeta.RecipeYoutubeMeta;
import com.cheftory.api.recipeinfo.youtubemeta.YoutubeMetaType;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.Nullable;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class RecipeHistoryOverview {
  // Recipe fields
  private UUID recipeId;
  private RecipeStatus recipeStatus;
  private Integer viewCount;
  private LocalDateTime recipeCreatedAt;
  private LocalDateTime recipeUpdatedAt;

  // RecipeHistory fields
  private LocalDateTime viewedAt;
  private Integer lastPlaySeconds;
  private UUID recipeCategoryId;

  // RecipeYoutubeMeta fields
  private String videoTitle;
  private String videoId;
  private URI videoUri;
  private URI thumbnailUrl;
  private Integer videoSeconds;
  private YoutubeMetaType videoType;

  // RecipeDetailMeta fields
  private String description;
  private Integer servings;
  private Integer cookTime;

  // RecipeTag fields
  private List<String> tags;

  public static RecipeHistoryOverview of(
      Recipe recipe,
      RecipeHistory recipeHistory,
      RecipeYoutubeMeta youtubeMeta,
      @Nullable RecipeDetailMeta detailMeta,
      @Nullable List<RecipeTag> tags) {
    return RecipeHistoryOverview.builder()
        .recipeId(recipe.getId())
        .recipeStatus(recipe.getRecipeStatus())
        .viewCount(recipe.getViewCount())
        .recipeCreatedAt(recipe.getCreatedAt())
        .recipeUpdatedAt(recipe.getUpdatedAt())
        .viewedAt(recipeHistory.getViewedAt())
        .lastPlaySeconds(recipeHistory.getLastPlaySeconds())
        .recipeCategoryId(recipeHistory.getRecipeCategoryId())
        .videoTitle(youtubeMeta.getTitle())
        .videoId(youtubeMeta.getVideoId())
        .videoUri(youtubeMeta.getVideoUri())
        .thumbnailUrl(youtubeMeta.getThumbnailUrl())
        .videoSeconds(youtubeMeta.getVideoSeconds())
        .videoType(youtubeMeta.getType())
        .description(detailMeta == null ? null : detailMeta.getDescription())
        .servings(detailMeta == null ? null : detailMeta.getServings())
        .cookTime(detailMeta == null ? null : detailMeta.getCookTime())
        .tags(tags == null ? List.of() : tags.stream().map(RecipeTag::getTag).toList())
        .build();
  }
}
