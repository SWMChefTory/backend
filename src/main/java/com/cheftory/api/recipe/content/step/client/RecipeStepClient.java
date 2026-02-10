package com.cheftory.api.recipe.content.step.client;

import com.cheftory.api.recipe.content.step.client.dto.ClientRecipeStepsResponse;
import com.cheftory.api.recipe.content.step.exception.RecipeStepException;

/**
 * 외부 레시피 단계 생성 API와 통신하는 클라이언트 인터페이스
 */
public interface RecipeStepClient {
    /**
     * 외부 API를 호출하여 레시피 단계 정보를 가져옴
     *
     * @param fileUri 파일 URI
     * @param mimeType 파일 MIME 타입
     * @return 레시피 단계 정보 응답 DTO
     * @throws RecipeStepException API 호출 실패 시
     */
    ClientRecipeStepsResponse fetch(String fileUri, String mimeType) throws RecipeStepException;
}
