package com.cheftory.api.recipe.content.verify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 레시피 검증 클라이언트 요청 DTO
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class RecipeVerifyClientRequest {
    /**
     * 비디오 ID
     */
    @JsonProperty("video_id")
    private String videoId;

    /**
     * 요청 객체 생성
     *
     * @param videoId 비디오 ID
     * @return 요청 객체
     */
    public static RecipeVerifyClientRequest from(String videoId) {
        return new RecipeVerifyClientRequest(videoId);
    }
}
