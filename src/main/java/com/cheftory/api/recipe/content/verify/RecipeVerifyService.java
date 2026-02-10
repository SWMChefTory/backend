package com.cheftory.api.recipe.content.verify;

import com.cheftory.api.recipe.content.verify.client.RecipeVerifyClient;
import com.cheftory.api.recipe.content.verify.dto.RecipeVerifyClientResponse;
import com.cheftory.api.recipe.content.verify.exception.RecipeVerifyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 레시피 검증 도메인의 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
public class RecipeVerifyService {

    private final RecipeVerifyClient recipeVerifyClient;

    /**
     * 레시피 영상 검증
     *
     * <p>외부 클라이언트를 통해 비디오 ID에 해당하는 영상이 요리 영상인지 검증합니다.</p>
     *
     * @param videoId 유튜브 비디오 ID
     * @return 검증 결과 (파일 URI, MIME 타입)
     * @throws RecipeVerifyException 검증 실패 또는 서버 오류 시
     */
    public RecipeVerifyClientResponse verify(String videoId) throws RecipeVerifyException {
        return recipeVerifyClient.verify(videoId);
    }

    /**
     * 레시피 영상 리소스 정리
     *
     * <p>검증 과정에서 생성된 임시 파일 등의 리소스를 정리합니다.</p>
     *
     * @param fileUri 정리할 파일의 URI
     */
    public void cleanup(String fileUri) {
        recipeVerifyClient.cleanupVideo(fileUri);
    }
}
