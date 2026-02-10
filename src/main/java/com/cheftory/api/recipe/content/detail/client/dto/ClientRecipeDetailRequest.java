package com.cheftory.api.recipe.content.detail.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 레시피 상세 생성 외부 API 요청 DTO
 *
 * <p>외부 레시피 상세 생성 API에 상세 정보 추출을 요청하기 위한 DTO입니다.</p>
 *
 * @param videoId 비디오 ID
 * @param fileUri 파일 URI
 * @param mimeType 파일 MIME 타입
 */
public record ClientRecipeDetailRequest(
        @JsonProperty("video_id") String videoId,
        @JsonProperty("file_uri") String fileUri,
        @JsonProperty("mime_type") String mimeType) {

    /**
     * 요청 DTO 생성 팩토리 메서드
     *
     * @param videoId 비디오 ID
     * @param fileUri 파일 URI
     * @param mimeType 파일 MIME 타입
     * @return 요청 DTO 객체
     */
    public static ClientRecipeDetailRequest from(String videoId, String fileUri, String mimeType) {
        return new ClientRecipeDetailRequest(videoId, fileUri, mimeType);
    }
}
