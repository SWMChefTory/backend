package com.cheftory.api.recipe.dto;

import com.cheftory.api.recipe.content.detailMeta.entity.RecipeDetailMeta;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.entity.RecipeStatus;
import com.cheftory.api.recipe.content.tag.entity.RecipeTag;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeMetaType;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
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
  private Long creditCost;

  public static RecipeOverview of(
      RecipeInfo recipe,
      RecipeYoutubeMeta youtubeMeta,
      RecipeDetailMeta detailMeta,
      List<RecipeTag> tags,
      Boolean isViewed) {

    return new RecipeOverview(
        recipe.getId(),
        recipe.getRecipeStatus(),
        recipe.getViewCount(),
        recipe.getCreatedAt(),
        recipe.getUpdatedAt(),
        youtubeMeta.getTitle(),
        youtubeMeta.getVideoId(),
        youtubeMeta.getVideoUri(),
        youtubeMeta.getThumbnailUrl(),
        youtubeMeta.getVideoSeconds(),
        youtubeMeta.getType(),
        detailMeta == null ? null : detailMeta.getDescription(),
        detailMeta == null ? null : detailMeta.getServings(),
        detailMeta == null ? null : detailMeta.getCookTime(),
        tags.stream().map(RecipeTag::getTag).toList(),
        isViewed,
        recipe.getCreditCost());
  }
}
