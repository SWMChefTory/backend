package com.cheftory.api.recipe.content.step.repository;

import com.cheftory.api.recipe.content.step.entity.RecipeStep;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;

/**
 * 레시피 단계 Repository 인터페이스
 */
public interface RecipeStepRepository {
    /**
     * 레시피 ID로 단계 목록 조회
     *
     * @param recipeId 레시피 ID
     * @param sort 정렬 조건
     * @return 정렬된 레시피 단계 목록
     */
    List<RecipeStep> finds(UUID recipeId, Sort sort);

    /**
     * 레시피 단계 목록 일괄 저장
     *
     * @param recipeSteps 저장할 단계 목록
     * @return 저장된 단계 목록
     */
    List<UUID> create(List<RecipeStep> recipeSteps);
}
