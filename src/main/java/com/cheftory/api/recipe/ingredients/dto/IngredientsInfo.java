package com.cheftory.api.recipe.ingredients.dto;

import com.cheftory.api.recipe.ingredients.entity.Ingredient;
import com.cheftory.api.recipe.ingredients.entity.RecipeIngredients;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class IngredientsInfo {
    private UUID ingredientsId;
    private List<Ingredient> ingredients;

    public static IngredientsInfo from(RecipeIngredients recipeIngredients) {
        return IngredientsInfo.builder()
                .ingredientsId(recipeIngredients.getId())
                .ingredients(recipeIngredients.getIngredients())
                .build();
    }
}
