package com.cheftory.api.recipe.content.verify.client;

import com.cheftory.api.recipe.content.verify.dto.RecipeVerifyClientResponse;
import com.cheftory.api.recipe.content.verify.exception.RecipeVerifyException;

/**
 * 레시피 검증 외부 클라이언트 인터페이스
 */
public interface RecipeVerifyClient {
    /**
     * 레시피 영상 검증 요청
     *
     * @param videoId 유튜브 비디오 ID
     * @return 검증 결과
     * @throws RecipeVerifyException 검증 실패 시
     */
    RecipeVerifyClientResponse verify(String videoId) throws RecipeVerifyException;

    /**
     * 영상 리소스 정리 요청
     *
     * @param fileUri 파일 URI
     */
    void cleanupVideo(String fileUri);
}
