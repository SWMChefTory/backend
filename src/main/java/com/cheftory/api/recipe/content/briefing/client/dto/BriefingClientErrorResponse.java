package com.cheftory.api.recipe.content.briefing.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 레시피 브리핑 생성 외부 API 에러 응답 DTO
 *
 * <p>외부 레시피 브리핑 생성 API 호출 실패 시 반환되는 에러 정보를 담습니다.</p>
 */
@Data
@NoArgsConstructor
public class BriefingClientErrorResponse {
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
