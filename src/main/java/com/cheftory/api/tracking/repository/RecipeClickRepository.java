package com.cheftory.api.tracking.repository;

import com.cheftory.api.tracking.entity.RecipeClick;

/**
 * 레시피 클릭 Repository 인터페이스.
 */
public interface RecipeClickRepository {
    /**
     * 레시피 클릭 기록 저장.
     *
     * @param recipeClick 레시피 클릭 엔티티
     */
    void save(RecipeClick recipeClick);
}
