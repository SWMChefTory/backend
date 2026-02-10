package com.cheftory.api.recipe.content.briefing.respotiory;

import com.cheftory.api.recipe.content.briefing.entity.RecipeBriefing;
import java.util.List;
import java.util.UUID;

/**
 * 레시피 브리핑 Repository 인터페이스
 */
public interface RecipeBriefingRepository {
    /**
     * 레시피 ID로 브리핑 목록 조회
     *
     * @param recipeId 레시피 ID
     * @return 레시피 브리핑 목록
     */
    List<RecipeBriefing> findAllByRecipeId(UUID recipeId);

    /**
     * 레시피 브리핑 목록 일괄 저장
     *
     * @param recipeBriefings 저장할 브리핑 목록
     */
    void saveAll(List<RecipeBriefing> recipeBriefings);
}
