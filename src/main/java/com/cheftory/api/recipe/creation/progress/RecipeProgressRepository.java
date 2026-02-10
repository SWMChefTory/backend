package com.cheftory.api.recipe.creation.progress;

import com.cheftory.api.recipe.creation.progress.entity.RecipeProgress;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 레시피 생성 진행 상태 JPA 리포지토리.
 */
public interface RecipeProgressRepository extends JpaRepository<RecipeProgress, UUID> {
    /**
     * 레시피 ID로 진행 상태 목록을 조회합니다.
     *
     * @param recipeId 레시피 ID
     * @param sort 정렬
     * @return 진행 상태 목록
     */
    List<RecipeProgress> findAllByRecipeId(UUID recipeId, Sort sort);
}
