package com.cheftory.api.recipe.creation;

import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.youtubemeta.RecipeYoutubeMetaService;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeVideoInfo;
import com.cheftory.api.recipe.creation.identify.RecipeIdentifyService;
import com.cheftory.api.recipe.creation.identify.exception.RecipeIdentifyException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 레시피 생성 트랜잭션 서비스.
 *
 * <p>레시피 생성 관련 트랜잭션을 처리합니다.</p>
 */
@Service
@RequiredArgsConstructor
public class RecipeCreationTxService {
    private final RecipeInfoService recipeInfoService;
    private final RecipeIdentifyService recipeIdentifyService;
    private final RecipeYoutubeMetaService recipeYoutubeMetaService;

    /**
     * YouTube 비디오 정보를 사용하여 레시피를 생성합니다.
     *
     * <p>레시피 정보, 식별자, YouTube 메타데이터를 하나의 트랜잭션으로 생성합니다.</p>
     *
     * @param videoInfo YouTube 비디오 정보
     * @return 생성된 레시피 정보
     * @throws RecipeIdentifyException 레시피 식별 정보 생성 실패 시 (이미 진행 중인 경우)
     */
    @Transactional
    public RecipeInfo createWithIdentifyWithVideoInfo(YoutubeVideoInfo videoInfo) throws RecipeIdentifyException {
        RecipeInfo recipeInfo = recipeInfoService.create();
        recipeIdentifyService.create(videoInfo.getVideoUri());
        recipeYoutubeMetaService.create(videoInfo, recipeInfo.getId());
        return recipeInfo;
    }
}
