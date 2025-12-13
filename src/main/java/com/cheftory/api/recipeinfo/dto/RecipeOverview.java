package com.cheftory.api.recipeinfo.model;

import com.cheftory.api.recipeinfo.detailMeta.RecipeDetailMeta;
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

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class RecipeOverview {
  private UUID recipeId;
  private RecipeStatus recipeStatus;
  private Integer viewCount;
  private LocalDateTime recipeCreatedAt;
  private LocalDateTime recipeUpdatedAt;

  private String videoTitle;
  private String videoId;
  private URI videoUri;
  private URI thumbnailUrl;
  private Integer videoSeconds;
  private YoutubeMetaType videoType;

  private String description;
  private Integer servings;
  private Integer cookTime;

  private List<String> tags;

  private Boolean isViewed;

  public static RecipeOverview of(
      Recipe recipe,
      RecipeYoutubeMeta youtubeMeta,
      RecipeDetailMeta detailMeta,
      List<RecipeTag> tags,
      Boolean isViewed) {
    return RecipeOverview.builder()
        .recipeId(recipe.getId())
        .recipeStatus(recipe.getRecipeStatus())
        .viewCount(recipe.getViewCount())
        .recipeCreatedAt(recipe.getCreatedAt())
        .recipeUpdatedAt(recipe.getUpdatedAt())
        .videoTitle(youtubeMeta.getTitle())
        .videoId(youtubeMeta.getVideoId())
        .videoUri(youtubeMeta.getVideoUri())
        .thumbnailUrl(youtubeMeta.getThumbnailUrl())
        .videoSeconds(youtubeMeta.getVideoSeconds())
        .videoType(youtubeMeta.getType())
        .description(detailMeta == null ? null : detailMeta.getDescription())
        .servings(detailMeta == null ? null : detailMeta.getServings())
        .cookTime(detailMeta == null ? null : detailMeta.getCookTime())
        .tags(tags.stream().map(RecipeTag::getTag).toList())
        .isViewed(isViewed)
        .build();
  }
}
