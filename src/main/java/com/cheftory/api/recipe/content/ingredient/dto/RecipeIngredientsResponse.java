package com.cheftory.api.recipe.content.ingredient.dto;

import com.cheftory.api.recipe.content.ingredient.entity.RecipeIngredient;
import java.util.List;

/**
 * 레시피 재료 목록 응답 DTO
 */
public record RecipeIngredientsResponse(List<Ingredient> ingredients) {

    /**
     * 개별 재료 정보 레코드
     */
    private record Ingredient(String name, String unit, Integer amount) {

        /**
         * 엔티티로부터 DTO 생성
         *
         * @param recipeIngredient 재료 엔티티
         * @return 재료 정보 DTO
         */
        private static Ingredient from(RecipeIngredient recipeIngredient) {
            return new Ingredient(recipeIngredient.getName(), recipeIngredient.getUnit(), recipeIngredient.getAmount());
        }
    }

    /**
     * 엔티티 목록으로부터 응답 DTO 생성
     *
     * @param recipeIngredients 재료 엔티티 목록
     * @return 레시피 재료 목록 응답 DTO
     */
    public static RecipeIngredientsResponse from(List<RecipeIngredient> recipeIngredients) {
        return new RecipeIngredientsResponse(
                recipeIngredients.stream().map(Ingredient::from).toList());
    }
}
