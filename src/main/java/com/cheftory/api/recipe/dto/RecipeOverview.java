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

/**
 * 레시피 개요 정보
 *
 * <p>레시피 목록 조회에 필요한 주요 정보를 담습니다.</p>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class RecipeOverview {
    /**
     * 레시피 ID
     */
    private UUID recipeId;
    /**
     * 레시피 상태
     */
    private RecipeStatus recipeStatus;
    /**
     * 조회수
     */
    private Integer viewCount;
    /**
     * 레시피 생성 일시
     */
    private LocalDateTime recipeCreatedAt;
    /**
     * 레시피 수정 일시
     */
    private LocalDateTime recipeUpdatedAt;

    /**
     * 비디오 제목
     */
    private String videoTitle;
    /**
     * 채널 제목
     */
    private String channelTitle;
    /**
     * 비디오 ID
     */
    private String videoId;
    /**
     * 비디오 URI
     */
    private URI videoUri;
    /**
     * 썸네일 URL
     */
    private URI thumbnailUrl;
    /**
     * 비디오 재생시간 (초)
     */
    private Integer videoSeconds;
    /**
     * 비디오 타입
     */
    private YoutubeMetaType videoType;

    /**
     * 레시피 설명
     */
    private String description;
    /**
     * 인분
     */
    private Integer servings;
    /**
     * 조리 시간 (분)
     */
    private Integer cookTime;

    /**
     * 태그 목록
     */
    private List<String> tags;

    /**
     * 조회 여부
     */
    private Boolean isViewed;
    /**
     * 크레딧 비용
     */
    private Long creditCost;

    /**
     * 엔티티들로부터 RecipeOverview 생성
     *
     * @param recipe 레시피 정보 엔티티
     * @param youtubeMeta YouTube 메타데이터 엔티티
     * @param detailMeta 레시피 상세 메타데이터 엔티티
     * @param tags 레시피 태그 목록
     * @param isViewed 조회 여부
     * @return 레시피 개요 객체
     */
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
                youtubeMeta.getChannelTitle(),
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
