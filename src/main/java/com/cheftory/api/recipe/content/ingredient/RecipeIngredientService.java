package com.cheftory.api.recipe.content.ingredient;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.detail.entity.RecipeDetail.Ingredient;
import com.cheftory.api.recipe.content.ingredient.entity.RecipeIngredient;
import com.cheftory.api.recipe.content.ingredient.repository.RecipeIngredientRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 레시피 재료 도메인의 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeIngredientService {
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final Clock clock;

    /**
     * 레시피 재료 목록 생성
     *
     * @param recipeId 레시피 ID
     * @param ingredients 재료 정보 목록
     */
    public void create(UUID recipeId, List<Ingredient> ingredients) {
        List<RecipeIngredient> recipeIngredients = ingredients.stream()
                .map(ingredient -> RecipeIngredient.create(
                        ingredient.name(), ingredient.unit(), ingredient.amount(), recipeId, clock))
                .toList();
        recipeIngredientRepository.create(recipeIngredients);
    }

    /**
     * 레시피 ID로 재료 목록 조회
     *
     * @param recipeId 레시피 ID
     * @return 재료 목록
     */
    public List<RecipeIngredient> gets(UUID recipeId) {
        return recipeIngredientRepository.finds(recipeId);
    }
}
