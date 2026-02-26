package com.cheftory.api.recipe.content.verify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 레시피 검증 클라이언트 응답 DTO
 *
 * @param fileUri 파일 URI
 * @param mimeType MIME 타입
 */
public record RecipeVerifyClientResponse(
        @JsonProperty("file_uri") String fileUri,
        @JsonProperty("mime_type") String mimeType) {}
