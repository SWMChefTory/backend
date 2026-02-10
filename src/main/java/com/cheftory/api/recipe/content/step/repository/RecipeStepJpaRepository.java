package com.cheftory.api.recipe.content.step.repository;

import com.cheftory.api.recipe.content.step.entity.RecipeStep;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 레시피 단계 JPA Repository
 */
public interface RecipeStepJpaRepository extends JpaRepository<RecipeStep, UUID> {
    /**
     * 레시피 ID로 단계 목록 조회
     *
     * @param recipeId 레시피 ID
     * @param sort 정렬 조건
     * @return 정렬된 레시피 단계 목록
     */
    List<RecipeStep> findAllByRecipeId(UUID recipeId, Sort sort);
}
