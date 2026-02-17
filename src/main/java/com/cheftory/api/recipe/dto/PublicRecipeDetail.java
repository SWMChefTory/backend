package com.cheftory.api.recipe.dto;

import com.cheftory.api.recipe.content.briefing.entity.RecipeBriefing;
import com.cheftory.api.recipe.content.detailMeta.entity.RecipeDetailMeta;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.ingredient.entity.RecipeIngredient;
import com.cheftory.api.recipe.content.step.entity.RecipeStep;
import com.cheftory.api.recipe.content.tag.entity.RecipeTag;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 공개 레시피 상세용 DTO
 *
 * @param recipeId 레시피 ID
 * @param title 레시피 제목 (fallback: 유튜브 제목)
 * @param description 레시피 설명
 * @param thumbnailUrl 썸네일 URL
 * @param cookTime 조리 시간 (분)
 * @param servings 인분
 * @param viewCount 조회수
 * @param videoId 비디오 ID
 * @param videoTitle 비디오 제목
 * @param channelTitle 채널명
 * @param videoSeconds 비디오 길이 (초)
 * @param ingredients 재료 목록
 * @param steps 조리 단계 (details[], start 제외)
 * @param tags 태그 목록
 * @param briefings 브리핑 목록 (content 문자열만)
 * @param creditCost 크레딧 비용
 * @param createdAt 생성 일시
 * @param updatedAt 수정 일시
 */
public record PublicRecipeDetail(
        @JsonProperty("recipe_id") UUID recipeId,
        @JsonProperty("title") String title,
        @JsonProperty("description") String description,
        @JsonProperty("thumbnail_url") URI thumbnailUrl,
        @JsonProperty("cook_time") Integer cookTime,
        @JsonProperty("servings") Integer servings,
        @JsonProperty("view_count") Integer viewCount,
        @JsonProperty("video_id") String videoId,
        @JsonProperty("video_title") String videoTitle,
        @JsonProperty("channel_title") String channelTitle,
        @JsonProperty("video_seconds") Integer videoSeconds,
        @JsonProperty("ingredients") List<PublicIngredient> ingredients,
        @JsonProperty("steps") List<PublicStep> steps,
        @JsonProperty("tags") List<String> tags,
        @JsonProperty("briefings") List<String> briefings,
        @JsonProperty("credit_cost") Long creditCost,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @JsonProperty("updated_at") LocalDateTime updatedAt) {

    public static PublicRecipeDetail of(
            RecipeInfo recipe,
            RecipeDetailMeta detailMeta,
            RecipeYoutubeMeta youtubeMeta,
            List<RecipeIngredient> ingredients,
            List<RecipeStep> steps,
            List<RecipeTag> tags,
            List<RecipeBriefing> briefings) {

        String title = detailMeta == null ? null : detailMeta.getTitle();

        return new PublicRecipeDetail(
                recipe.getId(),
                (title != null && !title.isBlank()) ? title : youtubeMeta.getTitle(),
                detailMeta == null ? null : detailMeta.getDescription(),
                youtubeMeta.getThumbnailUrl(),
                detailMeta == null ? null : detailMeta.getCookTime(),
                detailMeta == null ? null : detailMeta.getServings(),
                recipe.getViewCount(),
                youtubeMeta.getVideoId(),
                youtubeMeta.getTitle(),
                youtubeMeta.getChannelTitle(),
                youtubeMeta.getVideoSeconds(),
                ingredients.stream().map(PublicIngredient::from).toList(),
                steps.stream().map(PublicStep::from).toList(),
                tags.stream().map(RecipeTag::getTag).toList(),
                briefings.stream().map(RecipeBriefing::getContent).toList(),
                recipe.getCreditCost(),
                recipe.getCreatedAt(),
                recipe.getUpdatedAt());
    }
}
