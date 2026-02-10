package com.cheftory.api.recipe.content.detail;

import com.cheftory.api.recipe.content.detail.client.RecipeDetailClient;
import com.cheftory.api.recipe.content.detail.entity.RecipeDetail;
import com.cheftory.api.recipe.exception.RecipeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 레시피 상세 정보 서비스.
 *
 * <p>외부 클라이언트를 통해 레시피 상세 정보를 조회합니다.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeDetailService {

    private final RecipeDetailClient client;

    /**
     * 외부 클라이언트를 통해 레시피 상세 정보를 조회합니다.
     *
     * @param videoId YouTube 비디오 ID
     * @param fileUri 파일 URI
     * @param mimeType 파일 MIME 타입
     * @return 레시피 상세 정보
     * @throws RecipeException 레시피 상세 정보 조회 실패 시
     */
    public RecipeDetail getRecipeDetails(String videoId, String fileUri, String mimeType) throws RecipeException {
        return client.fetch(videoId, fileUri, mimeType).toRecipeDetail();
    }
}
