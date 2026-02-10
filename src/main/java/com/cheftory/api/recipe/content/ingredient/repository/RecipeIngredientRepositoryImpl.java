package com.cheftory.api.recipe.content.ingredient.repository;

import com.cheftory.api._common.aspect.DbThrottled;
import com.cheftory.api.recipe.content.ingredient.entity.RecipeIngredient;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 레시피 재료 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class RecipeIngredientRepositoryImpl implements RecipeIngredientRepository {

    private final RecipeIngredientJpaRepository repository;

    /**
     * 레시피 ID로 재료 목록 조회
     *
     * @param recipeId 레시피 ID
     * @return 재료 목록
     */
    @Override
    public List<RecipeIngredient> finds(UUID recipeId) {
        return repository.findAllByRecipeId(recipeId);
    }

    /**
     * 레시피 재료 목록 일괄 저장
     *
     * @param recipeIngredients 저장할 재료 목록
     */
    @DbThrottled
    @Override
    public void create(List<RecipeIngredient> recipeIngredients) {
        repository.saveAll(recipeIngredients);
    }
}
