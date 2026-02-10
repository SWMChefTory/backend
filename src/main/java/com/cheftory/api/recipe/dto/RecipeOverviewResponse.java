package com.cheftory.api.recipe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.List;

/**
 * 레시피 개요 응답 DTO
 *
 * @param recipeId 레시피 ID
 * @param recipeTitle 레시피 제목
 * @param tags 태그 목록
 * @param isViewed 조회 여부
 * @param description 레시피 설명
 * @param servings 인분
 * @param cookingTime 조리 시간 (분)
 * @param videoId 비디오 ID
 * @param channelTitle 채널 제목
 * @param count 조회수
 * @param videoUrl 비디오 URL
 * @param videoType 비디오 타입
 * @param thumbnailUrl 썸네일 URL
 * @param videoSeconds 비디오 재생시간 (초)
 * @param creditCost 크레딧 비용
 */
public record RecipeOverviewResponse(
        @JsonProperty("recipe_id") String recipeId,
        @JsonProperty("recipe_title") String recipeTitle,
        @JsonProperty("tags") List<Tag> tags,
        @JsonProperty("is_viewed") Boolean isViewed,
        @JsonProperty("description") String description,
        @JsonProperty("servings") Integer servings,
        @JsonProperty("cooking_time") Integer cookingTime,
        @JsonProperty("video_id") String videoId,
        @JsonProperty("channel_title") String channelTitle,
        @JsonProperty("count") Integer count,
        @JsonProperty("video_url") String videoUrl,
        @JsonProperty("video_type") String videoType,
        @JsonProperty("video_thumbnail_url") URI thumbnailUrl,
        @JsonProperty("video_seconds") Integer videoSeconds,
        @JsonProperty("credit_cost") Long creditCost) {

    /**
     * RecipeOverview로부터 응답 DTO 생성
     *
     * @param recipe 레시피 개요 객체
     * @return 레시피 개요 응답 DTO
     */
    public static RecipeOverviewResponse of(RecipeOverview recipe) {
        return new RecipeOverviewResponse(
                recipe.getRecipeId().toString(),
                recipe.getVideoTitle(),
                recipe.getTags().stream().map(Tag::new).toList(),
                recipe.getIsViewed(),
                recipe.getDescription(),
                recipe.getServings(),
                recipe.getCookTime(),
                recipe.getVideoId(),
                recipe.getChannelTitle(),
                recipe.getViewCount(),
                recipe.getVideoUri().toString(),
                recipe.getVideoType().name(),
                recipe.getThumbnailUrl(),
                recipe.getVideoSeconds(),
                recipe.getCreditCost());
    }

    /**
     * 태그 응답 레코드
     *
     * @param name 태그 이름
     */
    private record Tag(@JsonProperty("name") String name) {}
}
