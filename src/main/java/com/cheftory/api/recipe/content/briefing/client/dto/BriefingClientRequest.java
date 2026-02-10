package com.cheftory.api.recipe.content.briefing.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 레시피 브리핑 생성 외부 API 요청 DTO
 *
 * <p>외부 레시피 브리핑 생성 API에 브리핑 추출을 요청하기 위한 DTO입니다.</p>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class BriefingClientRequest {
    /**
     * 비디오 ID
     */
    @JsonProperty("video_id")
    private String videoId;

    /**
     * 요청 DTO 생성 팩토리 메서드
     *
     * @param videoId 비디오 ID
     * @return 요청 DTO 객체
     */
    public static BriefingClientRequest from(String videoId) {
        return new BriefingClientRequest(videoId);
    }
}
