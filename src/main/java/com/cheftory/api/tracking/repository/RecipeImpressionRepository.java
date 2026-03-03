package com.cheftory.api.tracking.repository;

import com.cheftory.api.tracking.entity.RecipeImpression;
import java.util.List;

/**
 * 레시피 노출 Repository 인터페이스.
 */
public interface RecipeImpressionRepository {
    /**
     * 레시피 노출 기록 배치 저장.
     *
     * @param recipeImpressions 레시피 노출 엔티티 목록
     */
    void saveAll(List<RecipeImpression> recipeImpressions);
}
