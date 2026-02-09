package com.cheftory.api.recipe.content.ingredient.repository;

import com.cheftory.api.recipe.content.ingredient.entity.RecipeIngredient;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 레시피 재료 JPA Repository
 */
public interface RecipeIngredientJpaRepository extends JpaRepository<RecipeIngredient, UUID> {
    /**
     * 레시피 ID로 모든 재료 조회
     *
     * @param recipeId 레시피 ID
     * @return 재료 엔티티 목록
     */
    List<RecipeIngredient> findAllByRecipeId(UUID recipeId);
}
