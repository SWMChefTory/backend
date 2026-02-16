package com.cheftory.api.recipe.content.step.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 외부 API에 레시피 단계 추출을 요청하는 DTO
 *
 * @param fileUri 파일 URI
 * @param mimeType 파일 MIME 타입
 */
public record ClientRecipeStepsRequest(
        @JsonProperty("file_uri") String fileUri,
        @JsonProperty("mime_type") String mimeType) {

    /**
     * 요청 DTO 생성 팩토리 메서드
     *
     * @param fileUri 파일 URI
     * @param mimeType 파일 MIME 타입
     * @return 요청 DTO 객체
     */
    public static ClientRecipeStepsRequest from(String fileUri, String mimeType) {
        return new ClientRecipeStepsRequest(fileUri, mimeType);
    }
}
