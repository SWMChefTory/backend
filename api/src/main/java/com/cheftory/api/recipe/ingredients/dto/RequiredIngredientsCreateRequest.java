package com.cheftory.api.recipe.ingredients.dto;

import com.cheftory.api.recipe.info.entity.RecipeInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class RequiredIngredientsCreateRequest {
    private final RecipeInfo recipeInfo;
    private final String content;
}
