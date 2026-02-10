package com.cheftory.api.recipe.content.verify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 레시피 검증 클라이언트 에러 응답 DTO
 */
@Data
@NoArgsConstructor
public class RecipeVerifyClientErrorResponse {
    /**
     * 에러 코드
     */
    @JsonProperty("error_code")
    private String errorCode;

    /**
     * 에러 메시지
     */
    @JsonProperty("error_message")
    private String errorMessage;
}
