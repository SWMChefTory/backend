package com.cheftory.api.recipe.content.ingredient.repository;

import com.cheftory.api.recipe.content.ingredient.entity.RecipeIngredient;
import java.util.List;
import java.util.UUID;

/**
 * 레시피 재료 Repository 인터페이스
 */
public interface RecipeIngredientRepository {
    /**
     * 레시피 ID로 재료 목록 조회
     *
     * @param recipeId 레시피 ID
     * @return 재료 목록
     */
    List<RecipeIngredient> finds(UUID recipeId);

    /**
     * 레시피 재료 목록 일괄 저장
     *
     * @param recipeIngredients 저장할 재료 목록
     */
    void create(List<RecipeIngredient> recipeIngredients);
}
