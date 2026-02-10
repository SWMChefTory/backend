package com.cheftory.api.recipe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.UUID;

/**
 * 레시피 생성 요청 DTO.
 *
 * @param videoUrl 레시피 비디오 URL (YouTube 등)
 */
public record RecipeCreateRequest(@JsonProperty("video_url") URI videoUrl) {
    /**
     * 크롤러 타겟으로 변환합니다.
     *
     * @return 크롤러 타겟
     */
    public RecipeCreationTarget toCrawlerTarget() {
        return new RecipeCreationTarget.Crawler(videoUrl);
    }

    /**
     * 사용자 타겟으로 변환합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자 타겟
     */
    public RecipeCreationTarget toUserTarget(UUID userId) {
        return new RecipeCreationTarget.User(videoUrl, userId);
    }
}
