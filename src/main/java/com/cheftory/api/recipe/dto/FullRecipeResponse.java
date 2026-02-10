package com.cheftory.api.recipe.dto;

import com.cheftory.api.recipe.bookmark.entity.RecipeBookmark;
import com.cheftory.api.recipe.content.briefing.entity.RecipeBriefing;
import com.cheftory.api.recipe.content.detailMeta.entity.RecipeDetailMeta;
import com.cheftory.api.recipe.content.info.entity.RecipeStatus;
import com.cheftory.api.recipe.content.ingredient.entity.RecipeIngredient;
import com.cheftory.api.recipe.content.step.entity.RecipeStep;
import com.cheftory.api.recipe.content.tag.entity.RecipeTag;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgress;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 전체 레시피 정보 응답 DTO
 *
 * <p>레시피의 모든 세부 정보를 포함하는 응답 DTO입니다.</p>
 *
 * @param recipeStatus 레시피 상태
 * @param videoInfo 비디오 정보
 * @param ingredients 재료 목록
 * @param recipeProgresses 진행 상태 목록
 * @param recipeSteps 조리 단계 목록
 * @param viewStatus 조회 상태
 * @param detailMeta 상세 메타데이터
 * @param tags 태그 목록
 * @param briefings 브리핑 목록
 * @param creditCost 크레딧 비용
 */
public record FullRecipeResponse(
        @JsonProperty("recipe_status") RecipeStatus recipeStatus,
        @JsonProperty("video_info") VideoInfo videoInfo,
        @JsonProperty("recipe_ingredient") List<Ingredient> ingredients,
        @JsonProperty("recipe_progresses") List<Progress> recipeProgresses,
        @JsonProperty("recipe_steps") List<Step> recipeSteps,
        @JsonProperty("view_status") ViewStatus viewStatus,
        @JsonProperty("recipe_detail_meta") DetailMeta detailMeta,
        @JsonProperty("recipe_tags") List<Tag> tags,
        @JsonProperty("recipe_briefings") List<Briefing> briefings,
        @JsonProperty("recipe_credit_cost") Long creditCost) {

    /**
     * FullRecipe로부터 응답 DTO 생성
     *
     * @param fullRecipe 전체 레시피 객체
     * @return 전체 레시피 응답 DTO
     */
    public static FullRecipeResponse of(FullRecipe fullRecipe) {
        return new FullRecipeResponse(
                fullRecipe.getRecipe().getRecipeStatus(),
                VideoInfo.from(fullRecipe.getRecipeYoutubeMeta()),
                fullRecipe.getRecipeIngredients().stream().map(Ingredient::from).toList(),
                fullRecipe.getRecipeProgresses().stream().map(Progress::from).toList(),
                fullRecipe.getRecipeSteps().stream().map(Step::from).toList(),
                fullRecipe.getRecipeBookmark() != null ? ViewStatus.from(fullRecipe.getRecipeBookmark()) : null,
                fullRecipe.getRecipeDetailMeta() != null
                        ? DetailMeta.from(fullRecipe.getRecipeDetailMeta(), fullRecipe.getRecipeYoutubeMeta().getTitle())
                        : null,
                fullRecipe.getRecipeTags().stream().map(Tag::from).toList(),
                fullRecipe.getRecipeBriefings().stream().map(Briefing::from).toList(),
                fullRecipe.getRecipe().getCreditCost());
    }

    /**
     * 상세 메타데이터 레코드
     *
     * @param title 레시피 제목
     * @param description 설명
     * @param servings 인분
     * @param cookingTime 조리 시간 (분)
     */
    private record DetailMeta(
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("servings") Integer servings,
            @JsonProperty("cookingTime") Integer cookingTime) {

        /**
         * RecipeDetailMeta로부터 변환
         *
         * @param detailMeta 상세 메타데이터 엔티티
         * @param fallbackTitle fallback 제목 (유튜브 영상 제목)
         * @return 상세 메타데이터 레코드
         */
        public static DetailMeta from(RecipeDetailMeta detailMeta, String fallbackTitle) {
            String title = detailMeta.getTitle();
            if (title == null || title.isBlank()) {
                title = fallbackTitle;
            }
            return new DetailMeta(title, detailMeta.getDescription(), detailMeta.getServings(), detailMeta.getCookTime());
        }
    }

    /**
     * 비디오 정보 레코드
     *
     * @param videoId 비디오 ID
     * @param title 비디오 제목
     * @param channelTitle 채널 제목
     * @param thumbnailUrl 썸네일 URL
     * @param videoSeconds 비디오 재생시간 (초)
     * @param videoType 비디오 타입
     */
    private record VideoInfo(
            @JsonProperty("video_id") String videoId,
            @JsonProperty("video_title") String title,
            @JsonProperty("channel_title") String channelTitle,
            @JsonProperty("video_thumbnail_url") URI thumbnailUrl,
            @JsonProperty("video_seconds") Integer videoSeconds,
            @JsonProperty("video_type") String videoType) {

        /**
         * RecipeYoutubeMeta로부터 변환
         *
         * @param youtubeMeta YouTube 메타데이터 엔티티
         * @return 비디오 정보 레코드
         */
        public static VideoInfo from(RecipeYoutubeMeta youtubeMeta) {
            return new VideoInfo(
                    youtubeMeta.getVideoId(),
                    youtubeMeta.getTitle(),
                    youtubeMeta.getChannelTitle(),
                    youtubeMeta.getThumbnailUrl(),
                    youtubeMeta.getVideoSeconds(),
                    youtubeMeta.getType().name());
        }
    }

    /**
     * 재료 레코드
     *
     * @param name 재료 이름
     * @param amount 양
     * @param unit 단위
     */
    private record Ingredient(
            @JsonProperty("name") String name,
            @JsonProperty("amount") Integer amount,
            @JsonProperty("unit") String unit) {

        /**
         * RecipeIngredient로부터 변환
         *
         * @param ingredient 재료 엔티티
         * @return 재료 레코드
         */
        public static Ingredient from(RecipeIngredient ingredient) {
            return new Ingredient(ingredient.getName(), ingredient.getAmount(), ingredient.getUnit());
        }
    }

    /**
     * 브리핑 레코드
     *
     * @param content 브리핑 내용
     */
    private record Briefing(@JsonProperty("content") String content) {

        /**
         * RecipeBriefing으로부터 변환
         *
         * @param briefing 브리핑 엔티티
         * @return 브리핑 레코드
         */
        public static Briefing from(RecipeBriefing briefing) {
            return new Briefing(briefing.getContent());
        }
    }

    /**
     * 조리 단계 레코드
     *
     * @param id 단계 ID
     * @param stepOrder 단계 순서
     * @param subtitle 소제목
     * @param details 상세 내용 목록
     * @param startTime 시작 시간 (초)
     */
    private record Step(
            @JsonProperty("id") UUID id,
            @JsonProperty("step_order") Integer stepOrder,
            @JsonProperty("subtitle") String subtitle,
            @JsonProperty("details") List<Detail> details,
            @JsonProperty("start_time") Double startTime) {

        /**
         * 단계 상세 레코드
         *
         * @param text 텍스트
         * @param start 시작 시간 (초)
         */
        private record Detail(@JsonProperty("text") String text, @JsonProperty("start") Double start) {

            /**
             * RecipeStep.Detail로부터 변환
             *
             * @param detail 단계 상세 엔티티
             * @return 단계 상세 레코드
             */
            public static Detail from(RecipeStep.Detail detail) {
                return new Detail(detail.getText(), detail.getStart());
            }
        }

        /**
         * RecipeStep으로부터 변환
         *
         * @param step 조리 단계 엔티티
         * @return 조리 단계 레코드
         */
        public static Step from(RecipeStep step) {
            return new Step(
                    step.getId(),
                    step.getStepOrder(),
                    step.getSubtitle(),
                    step.getDetails().stream().map(Detail::from).toList(),
                    step.getStart());
        }
    }

    /**
     * 조회 상태 레코드
     *
     * @param id 북마크 ID
     * @param viewedAt 조회 일시
     * @param lastPlaySeconds 마지막 재생 위치 (초)
     * @param createdAt 생성 일시
     */
    private record ViewStatus(
            @JsonProperty("id") UUID id,
            @JsonProperty("viewed_at") LocalDateTime viewedAt,
            @JsonProperty("last_play_seconds") Integer lastPlaySeconds,
            @JsonProperty("created_at") LocalDateTime createdAt) {

        /**
         * RecipeBookmark으로부터 변환
         *
         * @param bookmark 레시피 북마크 엔티티
         * @return 조회 상태 레코드
         */
        public static ViewStatus from(RecipeBookmark bookmark) {
            return new ViewStatus(
                    bookmark.getId(), bookmark.getViewedAt(), bookmark.getLastPlaySeconds(), bookmark.getCreatedAt());
        }
    }

    /**
     * 진행 상태 레코드
     *
     * @param step 진행 단계
     * @param detail 상세 단계
     */
    private record Progress(
            @JsonProperty("step") RecipeProgressStep step, @JsonProperty("detail") RecipeProgressDetail detail) {

        /**
         * RecipeProgress로부터 변환
         *
         * @param progress 진행 상태 엔티티
         * @return 진행 상태 레코드
         */
        public static Progress from(RecipeProgress progress) {
            return new Progress(progress.getStep(), progress.getDetail());
        }
    }

    /**
     * 태그 레코드
     *
     * @param name 태그 이름
     */
    private record Tag(@JsonProperty("name") String name) {

        /**
         * RecipeTag로부터 변환
         *
         * @param tag 태그 엔티티
         * @return 태그 레코드
         */
        public static Tag from(RecipeTag tag) {
            return new Tag(tag.getTag());
        }
    }
}
