package com.cheftory.api.recipe.dto;

import com.cheftory.api._common.cursor.CursorPage;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 카테고리별 레시피 목록 응답 DTO
 *
 * @param categorizedRecipes 카테고리화된 레시피 목록
 * @param hasNext 다음 페이지 존재 여부
 * @param nextCursor 다음 페이지 커서
 */
public record CategorizedRecipesResponse(
        @JsonProperty("categorized_recipes") List<CategorizedRecipe> categorizedRecipes,
        @JsonProperty("has_next") boolean hasNext,
        @JsonProperty("next_cursor") String nextCursor) {

    /**
     * CursorPage로부터 응답 DTO 생성
     *
     * @param slice 레시피 북마크 개요 커서 페이지
     * @return 카테고리별 레시피 목록 응답 DTO
     */
    public static CategorizedRecipesResponse from(CursorPage<RecipeBookmarkOverview> slice) {
        List<CategorizedRecipe> responses =
                slice.items().stream().map(CategorizedRecipe::from).toList();
        return new CategorizedRecipesResponse(responses, slice.hasNext(), slice.nextCursor());
    }

    /**
     * 카테고리화된 레시피 레코드
     *
     * @param viewedAt 조회 일시
     * @param lastPlaySeconds 마지막 재생 위치 (초)
     * @param recipeId 레시피 ID
     * @param recipeTitle 레시피 제목
     * @param thumbnailUrl 썸네일 URL
     * @param videoId 비디오 ID
     * @param channelTitle 채널 제목
     * @param videoSeconds 비디오 재생시간 (초)
     * @param categoryId 카테고리 ID
     * @param description 레시피 설명
     * @param cookTime 조리 시간 (분)
     * @param servings 인분
     * @param createdAt 생성 일시
     * @param tags 태그 목록
     * @param creditCost 크레딧 비용
     */
    private record CategorizedRecipe(
            @JsonProperty("viewed_at") LocalDateTime viewedAt,
            @JsonProperty("last_play_seconds") Integer lastPlaySeconds,
            @JsonProperty("recipe_id") UUID recipeId,
            @JsonProperty("recipe_title") String recipeTitle,
            @JsonProperty("video_thumbnail_url") URI thumbnailUrl,
            @JsonProperty("video_id") String videoId,
            @JsonProperty("channel_title") String channelTitle,
            @JsonProperty("video_seconds") Integer videoSeconds,
            @JsonProperty("category_id") UUID categoryId,
            @JsonProperty("description") String description,
            @JsonProperty("cook_time") Integer cookTime,
            @JsonProperty("servings") Integer servings,
            @JsonProperty("created_at") LocalDateTime createdAt,
            @JsonProperty("tags") List<Tag> tags,
            @JsonProperty("credit_cost") Long creditCost) {

        /**
         * RecipeBookmarkOverview로부터 변환
         *
         * @param info 레시피 북마크 개요
         * @return 카테고리화된 레시피 레코드
         */
        public static CategorizedRecipe from(RecipeBookmarkOverview info) {
            return new CategorizedRecipe(
                    info.getViewedAt(),
                    info.getLastPlaySeconds(),
                    info.getRecipeId(),
                    info.getVideoTitle(),
                    info.getThumbnailUrl(),
                    info.getVideoId(),
                    info.getChannelTitle(),
                    info.getVideoSeconds(),
                    info.getRecipeCategoryId(),
                    info.getDescription(),
                    info.getCookTime(),
                    info.getServings(),
                    info.getRecipeCreatedAt(),
                    info.getTags() != null
                            ? info.getTags().stream().map(Tag::from).toList()
                            : null,
                    info.getCreditCost());
        }
    }

    /**
     * 태그 레코드
     *
     * @param name 태그 이름
     */
    private record Tag(@JsonProperty("name") String name) {
        /**
         * 문자열로부터 태그 생성
         *
         * @param tag 태그 문자열
         * @return 태그 레코드
         */
        public static Tag from(String tag) {
            return new Tag(tag);
        }
    }
}
