package com.cheftory.api.recipe.dto;

import com.cheftory.api._common.cursor.CursorPage;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 최근 본 레시피 목록 응답 DTO
 *
 * @param recentRecipes 최근 본 레시피 목록
 * @param hasNext 다음 페이지 존재 여부
 * @param nextCursor 다음 페이지 커서
 */
public record RecentRecipesResponse(
        @JsonProperty("recent_recipes") List<RecentRecipeResponse> recentRecipes,
        @JsonProperty("has_next") boolean hasNext,
        @JsonProperty("next_cursor") String nextCursor) {

    /**
     * CursorPage로부터 응답 DTO 생성
     *
     * @param recentRecipes 레시피 북마크 개요 커서 페이지
     * @return 최근 본 레시피 목록 응답 DTO
     */
    public static RecentRecipesResponse from(CursorPage<RecipeBookmarkOverview> recentRecipes) {
        List<RecentRecipeResponse> responses =
                recentRecipes.items().stream().map(RecentRecipeResponse::from).toList();
        return new RecentRecipesResponse(responses, recentRecipes.hasNext(), recentRecipes.nextCursor());
    }

    /**
     * 최근 본 레시피 응답 레코드
     *
     * @param viewedAt 조회 일시
     * @param lastPlaySeconds 마지막 재생 위치 (초)
     * @param recipeId 레시피 ID
     * @param recipeTitle 레시피 제목
     * @param thumbnailUrl 썸네일 URL
     * @param videoId 비디오 ID
     * @param channelTitle 채널 제목
     * @param description 레시피 설명
     * @param cookTime 조리 시간 (분)
     * @param servings 인분
     * @param createdAt 생성 일시
     * @param videoSeconds 비디오 재생시간 (초)
     * @param tags 태그 목록
     * @param recipeStatus 레시피 상태
     * @param creditCost 크레딧 비용
     * @param videoType 비디오 타입
     */
    public record RecentRecipeResponse(
            @JsonProperty("viewed_at") LocalDateTime viewedAt,
            @JsonProperty("last_play_seconds") Integer lastPlaySeconds,
            @JsonProperty("recipe_id") UUID recipeId,
            @JsonProperty("recipe_title") String recipeTitle,
            @JsonProperty("video_thumbnail_url") URI thumbnailUrl,
            @JsonProperty("video_id") String videoId,
            @JsonProperty("channel_title") String channelTitle,
            @JsonProperty("description") String description,
            @JsonProperty("cook_time") Integer cookTime,
            @JsonProperty("servings") Integer servings,
            @JsonProperty("created_at") LocalDateTime createdAt,
            @JsonProperty("video_seconds") Integer videoSeconds,
            @JsonProperty("tags") List<Tag> tags,
            @JsonProperty("recipe_status") String recipeStatus,
            @JsonProperty("credit_cost") Long creditCost,
            @JsonProperty("video_type") String videoType) {

        /**
         * RecipeBookmarkOverview로부터 변환
         *
         * @param info 레시피 북마크 개요
         * @return 최근 본 레시피 응답 레코드
         */
        public static RecentRecipeResponse from(RecipeBookmarkOverview info) {
            return new RecentRecipeResponse(
                    info.getViewedAt(),
                    info.getLastPlaySeconds(),
                    info.getRecipeId(),
                    info.getVideoTitle(),
                    info.getThumbnailUrl(),
                    info.getVideoId(),
                    info.getChannelTitle(),
                    info.getDescription(),
                    info.getCookTime(),
                    info.getServings(),
                    info.getRecipeCreatedAt(),
                    info.getVideoSeconds(),
                    info.getTags() != null
                            ? info.getTags().stream().map(Tag::from).toList()
                            : null,
                    info.getRecipeStatus().name(),
                    info.getCreditCost(),
                    info.getVideoType().name());
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
}
