package com.cheftory.api.recipe.content.ingredient.dto;

import com.cheftory.api.recipe.content.ingredient.entity.RecipeIngredient;
import java.util.List;

public record RecipeIngredientsResponse(List<Ingredient> ingredients) {
    private record Ingredient(String name, String unit, Integer amount) {
        private static Ingredient from(RecipeIngredient recipeIngredient) {
            return new Ingredient(recipeIngredient.getName(), recipeIngredient.getUnit(), recipeIngredient.getAmount());
        }
    }

    public static RecipeIngredientsResponse from(List<RecipeIngredient> recipeIngredients) {
        return new RecipeIngredientsResponse(
                recipeIngredients.stream().map(Ingredient::from).toList());
    }
}
