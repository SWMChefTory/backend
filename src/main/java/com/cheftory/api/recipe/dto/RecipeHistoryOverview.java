package com.cheftory.api.recipe.dto;

import com.cheftory.api.recipe.content.detailMeta.entity.RecipeDetailMeta;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.entity.RecipeStatus;
import com.cheftory.api.recipe.content.tag.entity.RecipeTag;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeMetaType;
import com.cheftory.api.recipe.history.entity.RecipeHistory;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.Nullable;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class RecipeHistoryOverview {
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

  private List<String> tags;

  public static RecipeHistoryOverview of(
      RecipeInfo recipe,
      RecipeHistory recipeHistory,
      RecipeYoutubeMeta youtubeMeta,
      @Nullable RecipeDetailMeta detailMeta,
      @Nullable List<RecipeTag> tags) {

    return new RecipeHistoryOverview(
        recipe.getId(),
        recipe.getRecipeStatus(),
        recipe.getViewCount(),
        recipe.getCreatedAt(),
        recipe.getUpdatedAt(),
        recipeHistory.getViewedAt(),
        recipeHistory.getLastPlaySeconds(),
        recipeHistory.getRecipeCategoryId(),
        youtubeMeta.getTitle(),
        youtubeMeta.getVideoId(),
        youtubeMeta.getVideoUri(),
        youtubeMeta.getThumbnailUrl(),
        youtubeMeta.getVideoSeconds(),
        youtubeMeta.getType(),
        detailMeta == null ? null : detailMeta.getDescription(),
        detailMeta == null ? null : detailMeta.getServings(),
        detailMeta == null ? null : detailMeta.getCookTime(),
        tags == null ? List.of() : tags.stream().map(RecipeTag::getTag).toList());
  }
}
