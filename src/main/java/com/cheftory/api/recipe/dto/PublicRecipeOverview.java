package com.cheftory.api.recipe.dto;

import com.cheftory.api.recipe.content.detailMeta.entity.RecipeDetailMeta;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.tag.entity.RecipeTag;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 공개 레시피 목록용 DTO
 *
 * @param recipeId 레시피 ID
 * @param title 레시피 제목 (fallback: 유튜브 제목)
 * @param description 레시피 설명
 * @param thumbnailUrl 썸네일 URL
 * @param cookTime 조리 시간 (분)
 * @param servings 인분
 * @param viewCount 조회수
 * @param tags 태그 목록
 * @param channelTitle 채널명
 * @param createdAt 생성 일시
 */
public record PublicRecipeOverview(
        @JsonProperty("recipe_id") UUID recipeId,
        @JsonProperty("title") String title,
        @JsonProperty("description") String description,
        @JsonProperty("thumbnail_url") URI thumbnailUrl,
        @JsonProperty("cook_time") Integer cookTime,
        @JsonProperty("servings") Integer servings,
        @JsonProperty("view_count") Integer viewCount,
        @JsonProperty("tags") List<String> tags,
        @JsonProperty("channel_title") String channelTitle,
        @JsonProperty("created_at") LocalDateTime createdAt) {

    public static PublicRecipeOverview of(
            RecipeInfo recipe,
            RecipeYoutubeMeta youtubeMeta,
            RecipeDetailMeta detailMeta,
            List<RecipeTag> tags) {

        String title = detailMeta == null ? null : detailMeta.getTitle();

        return new PublicRecipeOverview(
                recipe.getId(),
                (title != null && !title.isBlank()) ? title : youtubeMeta.getTitle(),
                detailMeta == null ? null : detailMeta.getDescription(),
                youtubeMeta.getThumbnailUrl(),
                detailMeta == null ? null : detailMeta.getCookTime(),
                detailMeta == null ? null : detailMeta.getServings(),
                recipe.getViewCount(),
                tags.stream().map(RecipeTag::getTag).toList(),
                youtubeMeta.getChannelTitle(),
                recipe.getCreatedAt());
    }
}
